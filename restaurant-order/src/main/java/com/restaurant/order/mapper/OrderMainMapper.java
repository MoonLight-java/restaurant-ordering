package com.restaurant.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restaurant.order.entity.OrderMain;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface OrderMainMapper extends BaseMapper<OrderMain> {

    /**
     * 统计今日订单数
     */
    @Select("SELECT COUNT(*) FROM order_main WHERE DATE(create_time) = CURDATE() AND is_deleted = 0")
    int countTodayOrders();

    /**
     * 统计今日营收（已完成订单的实付金额之和）
     */
    @Select("SELECT COALESCE(SUM(pay_amount), 0) FROM order_main WHERE DATE(create_time) = CURDATE() AND status = 3 AND is_deleted = 0")
    BigDecimal sumTodayRevenue();

    /**
     * 统计待处理订单数（待确认 + 已确认 + 准备中）
     */
    @Select("SELECT COUNT(*) FROM order_main WHERE status IN (0, 1, 2) AND is_deleted = 0")
    int countPendingOrders();

    /**
     * 统计昨日订单数
     */
    @Select("SELECT COUNT(*) FROM order_main WHERE DATE(create_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY) AND is_deleted = 0")
    int countYesterdayOrders();
}
