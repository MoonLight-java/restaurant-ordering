package com.restaurant.payment.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderInfoDTO {
    private Long orderId;
    private String orderNo;
    private BigDecimal payAmount;
    private Integer status;
}
