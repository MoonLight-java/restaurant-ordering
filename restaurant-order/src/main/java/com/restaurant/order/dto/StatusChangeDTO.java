package com.restaurant.order.dto;

import lombok.Data;

@Data
public class StatusChangeDTO {

    /** 订单ID */
    private Long orderId;

    /** 新状态 */
    private Integer newStatus;

    /** 取消原因（仅取消时需要） */
    private String cancelReason;
}
