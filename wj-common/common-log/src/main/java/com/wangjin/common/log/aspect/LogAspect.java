package com.wangjin.common.log.aspect;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.wangjin.common.log.annotation.Log;
import com.wangjin.common.log.enums.BusinessStatus;
import com.wangjin.common.log.filter.SensitivePropertyFilter;
import com.wangjin.common.log.model.OperLog;
import com.wangjin.common.log.model.OperLogHandler;
import com.wangjin.common.security.util.SecurityUtils;
import com.wangjin.common.utils.IpUtils;
import com.wangjin.common.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 操作日志切面：拦截 {@link Log}，组装 {@link OperLog} 后交给 {@link OperLogHandler}。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private static final ThreadLocal<Long> TIME = new NamedThreadLocal<>("oper-log-cost");
    private static final String SENSITIVE_FILTER_ID = "operLogSensitiveFilter";

    private final ObjectProvider<OperLogHandler> operLogHandler;
    private final ObjectProvider<ObjectMapper> objectMapperProvider;

    private final SpelExpressionParser spelParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Pointcut("@annotation(com.wangjin.common.log.annotation.Log) || @within(com.wangjin.common.log.annotation.Log)")
    public void logPointcut() {
    }

    @Before("logPointcut()")
    public void doBefore() {
        TIME.set(System.currentTimeMillis());
    }

    @AfterReturning(pointcut = "logPointcut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        handle(joinPoint, null, result);
    }

    @AfterThrowing(pointcut = "logPointcut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handle(joinPoint, e, null);
    }

    private void handle(JoinPoint joinPoint, Exception e, Object result) {
        try {
            Log logAnno = resolveAnnotation(joinPoint);
            if (logAnno == null) {
                return;
            }

            OperLog operLog = new OperLog();
            operLog.setTitle(logAnno.title());
            operLog.setBusinessType(logAnno.businessType().name());
            operLog.setOperatorType(logAnno.operatorType().name());
            operLog.setStatus(BusinessStatus.SUCCESS.getCode());
            operLog.setOperateTime(LocalDateTime.now());

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            operLog.setMethod(signature.getDeclaringTypeName() + "." + signature.getName());

            HttpServletRequest request = ServletUtils.getRequest();
            if (request != null) {
                operLog.setRequestMethod(request.getMethod());
                operLog.setOperateUrl(StrUtil.sub(request.getRequestURI(), 0, 255));
                operLog.setOperateIp(IpUtils.getIpAddr(request));
            }

            fillOperator(operLog);

            if (logAnno.isSaveRequestData()) {
                operLog.setRequestParam(argsToString(joinPoint.getArgs(), logAnno.excludeParamNames()));
            }
            if (logAnno.isSaveResponseData() && result != null) {
                operLog.setJsonResult(StrUtil.sub(writeJson(result, logAnno.excludeParamNames()), 0, 2000));
            }
            if (e != null) {
                operLog.setStatus(BusinessStatus.FAIL.getCode());
                operLog.setErrorMsg(StrUtil.sub(e.getMessage(), 0, 2000));
            }

            Long start = TIME.get();
            if (start != null) {
                operLog.setCostTime(System.currentTimeMillis() - start);
            }

            if (StrUtil.isNotBlank(logAnno.bizNo())) {
                operLog.setBizNo(parseBizNo(logAnno.bizNo(), joinPoint));
            }

            dispatch(operLog);
        } catch (Exception ex) {
            log.warn("记录操作日志失败: {}", ex.getMessage());
        } finally {
            TIME.remove();
        }
    }

    private void fillOperator(OperLog operLog) {
        try {
            operLog.setOperatorName(SecurityUtils.getNickname());
            if (StrUtil.isBlank(operLog.getOperatorName())) {
                operLog.setOperatorName(SecurityUtils.getUsername());
            }
            operLog.setCreateBy(SecurityUtils.getUserId());
            Long deptId = SecurityUtils.getDeptId();
            if (deptId != null) {
                operLog.setDeptName(String.valueOf(deptId));
            }
        } catch (Throwable ignored) {
            // ignore
        }
    }

    private void dispatch(OperLog operLog) {
        OperLogHandler handler = operLogHandler.getIfAvailable();
        if (handler != null) {
            handler.handle(operLog);
            return;
        }
        log.info("oper-log title={} bizNo={} method={} status={} cost={}ms operator={}",
                operLog.getTitle(), operLog.getBizNo(), operLog.getMethod(),
                operLog.getStatus(), operLog.getCostTime(), operLog.getOperatorName());
    }

    private Log resolveAnnotation(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Log logAnno = method.getAnnotation(Log.class);
        if (logAnno != null) {
            return logAnno;
        }
        return joinPoint.getTarget().getClass().getAnnotation(Log.class);
    }

    private String parseBizNo(String spel, JoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String[] paramNames = parameterNameDiscoverer.getParameterNames(method);
            Object[] args = joinPoint.getArgs();
            EvaluationContext context = new StandardEvaluationContext();
            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }
            Expression expression = spelParser.parseExpression(spel);
            Object value = expression.getValue(context);
            return value == null ? null : String.valueOf(value);
        } catch (Exception ex) {
            log.debug("bizNo SpEL 解析失败: {} -> {}", spel, ex.getMessage());
            return null;
        }
    }

    private String argsToString(Object[] args, String[] excludes) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (Object arg : args) {
            if (arg == null || isFilterObject(arg)) {
                continue;
            }
            joiner.add(writeJson(arg, excludes));
        }
        return StrUtil.sub(joiner.toString(), 0, 2000);
    }

    private boolean isFilterObject(Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return MultipartFile.class.isAssignableFrom(clazz.getComponentType());
        }
        if (o instanceof Collection<?> collection) {
            return collection.stream().anyMatch(MultipartFile.class::isInstance);
        }
        if (o instanceof Map<?, ?> map) {
            return map.values().stream().anyMatch(MultipartFile.class::isInstance);
        }
        return o instanceof MultipartFile
                || o instanceof HttpServletRequest
                || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }

    private String writeJson(Object obj, String[] excludes) {
        try {
            ObjectMapper mapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
            FilterProvider filters = new SimpleFilterProvider()
                    .addFilter(SENSITIVE_FILTER_ID, new SensitivePropertyFilter(excludes))
                    .setFailOnUnknownId(false);
            String json = mapper.writer(filters).writeValueAsString(obj);
            return maskSensitiveKeys(json, excludes);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    private String maskSensitiveKeys(String json, String[] excludes) {
        if (json == null) {
            return null;
        }
        String result = json;
        String[] all = SensitivePropertyFilter.DEFAULT_EXCLUDES;
        for (String key : all) {
            result = result.replaceAll("(?i)(\"" + key + "\"\\s*:\\s*)(\"[^\"]*\"|[^,}\\]]+)", "$1\"***\"");
        }
        if (excludes != null) {
            for (String key : excludes) {
                if (StrUtil.isNotBlank(key)) {
                    result = result.replaceAll("(?i)(\"" + key + "\"\\s*:\\s*)(\"[^\"]*\"|[^,}\\]]+)", "$1\"***\"");
                }
            }
        }
        return result;
    }
}
