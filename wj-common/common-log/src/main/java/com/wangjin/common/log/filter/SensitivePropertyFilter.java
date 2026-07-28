package com.wangjin.common.log.filter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 操作日志参数敏感字段过滤。
 */
public class SensitivePropertyFilter extends SimpleBeanPropertyFilter implements PropertyFilter {

    public static final String[] DEFAULT_EXCLUDES = {
            "password", "oldPassword", "newPassword", "confirmPassword",
            "pwd", "secret", "token", "accessToken", "refreshToken"
    };

    private final Set<String> excludes = new HashSet<>();

    public SensitivePropertyFilter(String... extraExcludes) {
        Arrays.stream(DEFAULT_EXCLUDES).forEach(this::add);
        if (extraExcludes != null) {
            Arrays.stream(extraExcludes).forEach(this::add);
        }
    }

    private void add(String name) {
        if (name != null && !name.isBlank()) {
            excludes.add(name.toLowerCase(Locale.ROOT));
        }
    }

    @Override
    protected boolean include(BeanPropertyWriter writer) {
        return !excludes.contains(writer.getName().toLowerCase(Locale.ROOT));
    }

    @Override
    protected boolean include(PropertyWriter writer) {
        return !excludes.contains(writer.getName().toLowerCase(Locale.ROOT));
    }

    @Override
    public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider,
                                 PropertyWriter writer) throws Exception {
        if (include(writer)) {
            writer.serializeAsField(pojo, jgen, provider);
        }
    }
}
