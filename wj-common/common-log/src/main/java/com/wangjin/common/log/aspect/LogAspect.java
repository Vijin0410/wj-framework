package com.wangjin.common.log.aspect;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wangjin.common.log.annotation.Log;
import com.wangjin.common.log.enums.BusinessStatus;
import com.wangjin.common.log.model.OperLog;
import com.wangjin.common.log.model.OperLogHandler;
import com.wangjin.common.security.util.SecurityUtils;
import com.wangjin.common.utils.IpUtils;
import com.wangjin.common.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 操作日志切面。默认打日志；存在 {@link OperLogHandler} 时回调业务处理。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private static final ThreadLocal<Long> TIME = new NamedThreadLocal<>("oper-log-cost");

    private final ObjectProvider<OperLogHandler> operLogHandler;
    private final ObjectProvider<ObjectMapper> objectMapperProvider;

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

            try {
                operLog.setOperatorName(SecurityUtils.getNickname());
                if (StrUtil.isBlank(operLog.getOperatorName())) {
                    operLog.setOperatorName(SecurityUtils.getUsername());
                }
            } catch (Throwable ignored) {
                // security 可选
            }

            if (logAnno.isSaveRequestData()) {
                operLog.setRequestParam(argsToString(joinPoint.getArgs(), logAnno.excludeParamNames()));
            }
            if (logAnno.isSaveResponseData() && result != null) {
                operLog.setJsonResult(StrUtil.sub(writeJson(result), 0, 2000));
            }
            if (e != null) {
                operLog.setStatus(BusinessStatus.FAIL.getCode());
                operLog.setErrorMsg(StrUtil.sub(e.getMessage(), 0, 2000));
            }
            Long start = TIME.get();
            if (start != null) {
                operLog.setCostTime(System.currentTimeMillis() - start);
            }

            OperLogHandler handler = operLogHandler.getIfAvailable();
            if (handler != null) {
                handler.handle(operLog);
            } else {
                log.info("oper-log title={} method={} status={} cost={}ms",
                        operLog.getTitle(), operLog.getMethod(), operLog.getStatus(), operLog.getCostTime());
            }
        } catch (Exception ex) {
            log.warn("记录操作日志失败: {}", ex.getMessage());
        } finally {
            TIME.remove();
        }
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

    private String argsToString(Object[] args, String[] excludes) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringJoiner joiner = new StringJoiner(", ");
        for (Object arg : args) {
            if (arg == null || arg instanceof MultipartFile
                    || arg instanceof HttpServletRequest
                    || arg instanceof Collection && ((Collection<?>) arg).stream().anyMatch(MultipartFile.class::isInstance)
                    || arg instanceof Map) {
                continue;
            }
            joiner.add(writeJson(arg));
        }
        return StrUtil.sub(joiner.toString(), 0, 2000);
    }

    private String writeJson(Object obj) {
        try {
            ObjectMapper mapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}
