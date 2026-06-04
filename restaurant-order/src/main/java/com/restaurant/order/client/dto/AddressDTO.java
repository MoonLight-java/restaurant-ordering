package com.restaurant.order.client.dto;

import lombok.Data;

@Data
public class AddressDTO {

    private Long id;
    private Long userId;
    private String contactName;
    private String contactPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Integer isDefault;
}
