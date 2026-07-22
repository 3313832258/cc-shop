package com.ccshop.trade.controller;

import com.ccshop.common.core.Result;
import com.ccshop.trade.dto.PaymentVO;
import com.ccshop.trade.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trade/payment")
@RequiredArgsConstructor
@Tag(name = "支付")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/pay")
    @Operation(summary = "支付")
    public Result<PaymentVO> pay(
            @RequestParam Long orderId,
            @RequestParam(defaultValue = "alipay") String method) {
        return Result.success(paymentService.pay(orderId, method));
    }
}
