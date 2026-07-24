package com.wangjin.common.mq.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * MQ 消息事件基类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 消息 ID */
    private String id;
    /** 消息内容（JSON 或业务串） */
    private String data;

    public BaseEvent(String data) {
        this.data = data;
    }
}
