package com.wangjin.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Servlet 工具。
 */
public final class ServletUtils {

    private ServletUtils() {
    }

    public static ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : (ServletRequestAttributes) attributes;
    }

    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    public static HttpServletResponse getResponse() {
        ServletRequestAttributes attributes = getRequestAttributes();
        return attributes == null ? null : attributes.getResponse();
    }
}
