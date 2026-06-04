package com.restaurant.order.client;

import com.restaurant.common.dto.response.Result;
import com.restaurant.order.client.dto.AddressDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;

    private static final String USER_SERVICE_URL = "http://localhost:8082/api/user";

    /**
     * 根据地址ID获取地址详情
     */
    public AddressDTO getAddressById(Long userId, Long addressId) {
        List<AddressDTO> addresses = fetchAddresses(userId);
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }
        for (AddressDTO addr : addresses) {
            if (addressId.equals(addr.getId())) {
                return addr;
            }
        }
        return null;
    }

    /**
     * 获取用户默认地址
     */
    public AddressDTO getDefaultAddress(Long userId) {
        List<AddressDTO> addresses = fetchAddresses(userId);
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }
        for (AddressDTO addr : addresses) {
            if (addr.getIsDefault() != null && addr.getIsDefault() == 1) {
                return addr;
            }
        }
        return addresses.get(0);
    }

    private List<AddressDTO> fetchAddresses(Long userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", String.valueOf(userId));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Result<List<AddressDTO>>> response = restTemplate.exchange(
                    USER_SERVICE_URL + "/addresses",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Result<List<AddressDTO>>>() {}
            );

            Result<List<AddressDTO>> result = response.getBody();
            if (result != null && result.getCode() == 200) {
                return result.getData();
            }
        } catch (Exception e) {
            log.error("Failed to fetch addresses for userId={}", userId, e);
        }
        return null;
    }
}
