package com.restaurant.user.controller;

import com.restaurant.common.dto.response.Result;
import com.restaurant.user.entity.UserAddress;
import com.restaurant.user.service.AddressService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class AddressController {

    @Resource
    private AddressService addressService;

    @GetMapping("/addresses")
    public Result<List<UserAddress>> listAddresses(@RequestHeader("X-User-Id") Long userId) {
        List<UserAddress> list = addressService.listByUserId(userId);
        return Result.ok(list);
    }

    @PostMapping("/addresses")
    public Result<?> addAddress(@RequestHeader("X-User-Id") Long userId,
                                @RequestBody UserAddress addr) {
        addr.setUserId(userId);
        addressService.add(addr);
        return Result.ok();
    }

    @PutMapping("/addresses/{id}")
    public Result<?> updateAddress(@RequestHeader("X-User-Id") Long userId,
                                   @PathVariable Long id,
                                   @RequestBody UserAddress addr) {
        addr.setId(id);
        addr.setUserId(userId);
        addressService.update(addr);
        return Result.ok();
    }

    @DeleteMapping("/addresses/{id}")
    public Result<?> deleteAddress(@RequestHeader("X-User-Id") Long userId,
                                   @PathVariable Long id) {
        addressService.delete(id, userId);
        return Result.ok();
    }

    @PutMapping("/addresses/{id}/default")
    public Result<?> setDefaultAddress(@RequestHeader("X-User-Id") Long userId,
                                       @PathVariable Long id) {
        addressService.setDefault(id, userId);
        return Result.ok();
    }
}
