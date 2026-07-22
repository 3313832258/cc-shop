package com.ccshop.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ccshop.common.core.BusinessException;
import com.ccshop.common.core.UserContext;
import com.ccshop.user.dto.AddressRequest;
import com.ccshop.user.entity.Address;
import com.ccshop.user.mapper.AddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressMapper addressMapper;

    public List<Address> list() {
        LambdaQueryWrapper<Address> qw = new LambdaQueryWrapper<>();
        qw.eq(Address::getUserId, UserContext.getUserId())
          .orderByDesc(Address::getIsDefault)
          .orderByDesc(Address::getCreatedAt);
        return addressMapper.selectList(qw);
    }

    public Address add(AddressRequest req) {
        Address a = new Address();
        BeanUtils.copyProperties(req, a);
        a.setUserId(UserContext.getUserId());
        if (a.getIsDefault() == null) a.setIsDefault(0);
        addressMapper.insert(a);
        return a;
    }

    public Address update(Long id, AddressRequest req) {
        Address a = addressMapper.selectById(id);
        if (a == null || !a.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(400, "地址不存在");
        }
        BeanUtils.copyProperties(req, a);
        addressMapper.updateById(a);
        return a;
    }

    public void delete(Long id) {
        Address a = addressMapper.selectById(id);
        if (a == null || !a.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(400, "地址不存在");
        }
        addressMapper.deleteById(id);
    }

    public void setDefault(Long id) {
        // 先把当前用户所有地址重置为非默认
        List<Address> list = list();
        for (Address a : list) {
            a.setIsDefault(0);
            addressMapper.updateById(a);
        }
        // 再置目标为默认
        Address target = addressMapper.selectById(id);
        if (target == null || !target.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(400, "地址不存在");
        }
        target.setIsDefault(1);
        addressMapper.updateById(target);
    }

    /** 单条查询（供交易服务 Feign 调用） */
    public Address getById(Long id) {
        Address a = addressMapper.selectById(id);
        if (a == null || !a.getUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(400, "地址不存在");
        }
        return a;
    }
}
