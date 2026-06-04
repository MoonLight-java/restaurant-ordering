package com.restaurant.order.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderDTO {

    /** 地址ID */
    private Long addressId;

    /** 备注 */
    private String remark;

    /** 购物车项ID列表（从购物车中选择结账的项） */
    private List<Long> cartItemIds;
}
