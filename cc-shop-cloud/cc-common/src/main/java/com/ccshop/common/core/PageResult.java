package com.ccshop.common.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private List<T> records;
    private Long total;
    private Long page;
    private Long size;

    public PageResult(List<T> records, Long total) {
        this.records = records;
        this.total = total;
    }
}
