package com.restaurant.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restaurant.order.entity.OrderCart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderCartMapper extends BaseMapper<OrderCart> {
}
