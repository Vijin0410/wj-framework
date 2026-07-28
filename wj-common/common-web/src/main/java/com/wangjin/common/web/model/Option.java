package com.wangjin.common.web.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 下拉选项通用模型。
 */
@Data
@NoArgsConstructor
public class Option<T> {

    private T value;
    private String label;
    private String remark;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Option<T>> children;

    public Option(T value, String label) {
        this.value = value;
        this.label = label;
    }

    public Option(T value, String label, String remark) {
        this.value = value;
        this.label = label;
        this.remark = remark;
    }

    public Option(T value, String label, List<Option<T>> children) {
        this.value = value;
        this.label = label;
        this.children = children;
    }
}
