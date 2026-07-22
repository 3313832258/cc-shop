package com.ccshop.user.controller;

import com.ccshop.common.core.Result;
import com.ccshop.user.dto.AddressRequest;
import com.ccshop.user.entity.Address;
import com.ccshop.user.service.AddressService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/address")
@RequiredArgsConstructor
@Tag(name = "收货地址")
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/{id}")
    public Result<Address> getById(@PathVariable Long id) {
        return Result.success(addressService.getById(id));
    }

    @GetMapping
    public Result<List<Address>> list() {
        return Result.success(addressService.list());
    }

    @PostMapping
    public Result<Address> add(@Valid @RequestBody AddressRequest req) {
        return Result.success(addressService.add(req));
    }

    @PutMapping("/{id}")
    public Result<Address> update(@PathVariable Long id, @Valid @RequestBody AddressRequest req) {
        return Result.success(addressService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(id);
        return Result.success();
    }
}
