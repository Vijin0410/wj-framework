package com.wangjin.common.web.aspect;

import cn.hutool.core.util.StrUtil;
import com.wangjin.common.exception.BizException;
import com.wangjin.common.result.ResultCode;
import com.wangjin.common.security.context.UserContext;
import com.wangjin.common.web.annotation.PreventDuplicateResubmit;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 * 防重复提交切面（存在 RedissonClient 时生效）。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnClass(RedissonClient.class)
@ConditionalOnBean(RedissonClient.class)
public class DuplicateSubmitAspect {

    private static final String LOCK_PREFIX = "LOCK:RESUBMIT:";

    private final RedissonClient redissonClient;

    @Around("@annotation(anno)")
    public Object around(ProceedingJoinPoint pjp, PreventDuplicateResubmit anno) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return pjp.proceed();
        }
        HttpServletRequest request = attrs.getRequest();
        String userKey = String.valueOf(UserContext.getUserId());
        if (StrUtil.isBlank(userKey) || "null".equals(userKey) || "0".equals(userKey)) {
            userKey = request.getSession(true).getId();
        }
        String lockKey = LOCK_PREFIX + userKey + ":" + request.getMethod() + "-" + request.getRequestURI();
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = lock.tryLock(0, anno.expire(), TimeUnit.SECONDS);
        if (!locked) {
            throw new BizException(ResultCode.REPEAT_SUBMIT_ERROR);
        }
        return pjp.proceed();
    }
}
