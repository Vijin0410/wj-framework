package com.wangjin.common.utils;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * IP 工具。
 */
public final class IpUtils {

    private static final String[] HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    private IpUtils() {
    }

    public static String getIpAddr() {
        return getIpAddr(ServletUtils.getRequest());
    }

    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        for (String header : HEADERS) {
            String ip = request.getHeader(header);
            if (StrUtil.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
                // 多级代理取第一个
                int index = ip.indexOf(',');
                return index > 0 ? ip.substring(0, index).trim() : ip.trim();
            }
        }
        return request.getRemoteAddr();
    }
}
