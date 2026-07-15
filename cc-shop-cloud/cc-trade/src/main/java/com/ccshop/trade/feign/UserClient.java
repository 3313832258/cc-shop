package com.ccshop.trade.feign;

import com.ccshop.common.core.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cc-user", path = "/user")
public interface UserClient {

    @GetMapping("/address/{id}")
    Result<AddressDTO> getAddress(@PathVariable("id") Long id);

    @lombok.Data
    class AddressDTO {
        private Long id;
        private Long userId;
        private String receiverName;
        private String province;
        private String city;
        private String district;
        private String detail;
        private String phone;
        private Integer isDefault;
    }
}
