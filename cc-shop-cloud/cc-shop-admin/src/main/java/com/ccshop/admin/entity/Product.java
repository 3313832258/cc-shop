package com.ccshop.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Long brandId;
    private Long categoryId;
    private Long merchantId;
    private String images; // JSON 字符串
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
