package com.wangjin.common.mybatis.handler;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * JSON 字段通用 TypeHandler（基于 Jackson）。
 * 实体字段标注：@TableField(typeHandler = JsonTypeHandler.class)
 */
@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class JsonTypeHandler extends JacksonTypeHandler {

    public JsonTypeHandler(Class<?> type) {
        super(type);
    }
}
