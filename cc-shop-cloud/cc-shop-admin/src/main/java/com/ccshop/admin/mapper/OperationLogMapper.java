package com.ccshop.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ccshop.admin.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}