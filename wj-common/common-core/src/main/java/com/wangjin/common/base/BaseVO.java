package com.wangjin.common.base;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * VO 基类。业务 model 层 vo 包下的出参对象可继承此类。
 */
@Data
public class BaseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
