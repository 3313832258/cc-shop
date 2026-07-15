package com.ccshop.promotion.dto;

import lombok.Data;

@Data
public class ReceiveResult {
    private boolean success;
    private String message;

    public static ReceiveResult ok() {
        ReceiveResult r = new ReceiveResult();
        r.setSuccess(true);
        r.setMessage("领取成功");
        return r;
    }

    public static ReceiveResult fail(String msg) {
        ReceiveResult r = new ReceiveResult();
        r.setSuccess(false);
        r.setMessage(msg);
        return r;
    }
}
