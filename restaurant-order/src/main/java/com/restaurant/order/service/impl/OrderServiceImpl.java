package com.restaurant.order.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.restaurant.order.client.UserServiceClient;
import com.restaurant.order.client.dto.AddressDTO;
import com.restaurant.order.dto.CreateOrderDTO;
import com.restaurant.order.dto.OrderDetailDTO;
import com.restaurant.order.entity.OrderCart;
import com.restaurant.order.entity.OrderDetail;
import com.restaurant.order.entity.OrderMain;
import com.restaurant.order.mapper.OrderCartMapper;
import com.restaurant.order.mapper.OrderDetailMapper;
import com.restaurant.order.mapper.OrderMainMapper;
import com.restaurant.common.dto.mq.OrderMessage;
import com.restaurant.order.mq.OrderStatusProducer;
import com.restaurant.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMainMapper orderMainMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final OrderCartMapper orderCartMapper;
    private final UserServiceClient userServiceClient;
    private final OrderStatusProducer orderStatusProducer;

    // ======================== 用户端接口 ========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateOrderResult createOrder(Long userId, CreateOrderDTO dto) {
        // 1. 查询用户选中的购物车项
        List<OrderCart> cartItems;
        if (!CollectionUtils.isEmpty(dto.getCartItemIds())) {
            cartItems = orderCartMapper.selectBatchIds(dto.getCartItemIds());
        } else {
            // 如果未指定购物车项ID，则取用户全部购物车项
            LambdaQueryWrapper<OrderCart> cartWrapper = new LambdaQueryWrapper<>();
            cartWrapper.eq(OrderCart::getUserId, userId);
            cartItems = orderCartMapper.selectList(cartWrapper);
        }

        if (CollectionUtils.isEmpty(cartItems)) {
            throw new RuntimeException("购物车为空，无法创建订单");
        }

        // 2. 验证所有购物车项都属于当前用户
        for (OrderCart item : cartItems) {
            if (!userId.equals(item.getUserId())) {
                throw new RuntimeException("购物车项 " + item.getId() + " 不属于当前用户");
            }
        }

        // 3. 通过 HTTP 调用用户服务加载地址信息
        AddressDTO address = null;
        if (dto.getAddressId() != null) {
            address = userServiceClient.getAddressById(userId, dto.getAddressId());
        }
        if (address == null) {
            address = userServiceClient.getDefaultAddress(userId);
        }
        if (address == null) {
            throw new RuntimeException("未找到收货地址，请先添加地址");
        }

        // 4. 构建地址JSON（字段名映射：contactName -> receiverName, contactPhone -> receiverPhone）
        String fullAddress = address.getProvince() + address.getCity()
                + address.getDistrict() + address.getDetailAddress();
        Map<String, Object> addressMap = new LinkedHashMap<>();
        addressMap.put("id", address.getId());
        addressMap.put("receiverName", address.getContactName());
        addressMap.put("receiverPhone", address.getContactPhone());
        addressMap.put("province", address.getProvince());
        addressMap.put("city", address.getCity());
        addressMap.put("district", address.getDistrict());
        addressMap.put("detailAddress", address.getDetailAddress());
        addressMap.put("fullAddress", fullAddress);
        String addressJson = JSONUtil.toJsonStr(addressMap);

        // 5. 生成订单号：yyyyMMddHHmmss + 6位随机数
        String orderNo = generateOrderNo();

        // 6. 计算金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderCart item : cartItems) {
            BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            Integer quantity = item.getQuantity() != null ? item.getQuantity() : 0;
            BigDecimal itemTotal = unitPrice.multiply(new BigDecimal(quantity));
            totalAmount = totalAmount.add(itemTotal);
        }

        // 暂不实现优惠逻辑，优惠金额为0
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal payAmount = totalAmount.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }

        // 7. 插入订单主表
        OrderMain orderMain = new OrderMain();
        orderMain.setOrderNo(orderNo);
        orderMain.setUserId(userId);
        orderMain.setAddressJson(addressJson);
        orderMain.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        orderMain.setDiscountAmount(discountAmount);
        orderMain.setPayAmount(payAmount);
        orderMain.setStatus(OrderMain.STATUS_PENDING);
        orderMain.setRemark(dto.getRemark());
        orderMain.setCreateTime(LocalDateTime.now());
        orderMain.setUpdateTime(LocalDateTime.now());

        orderMainMapper.insert(orderMain);
        log.info("Order main inserted: orderNo={}, orderId={}", orderNo, orderMain.getId());

        // 8. 插入订单明细
        List<OrderDetail> details = new ArrayList<>();
        for (OrderCart item : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrderId(orderMain.getId());
            detail.setOrderNo(orderNo);
            detail.setDishId(item.getDishId());
            detail.setDishName(item.getDishName());
            detail.setDishImage(item.getDishImage());
            detail.setSpecJson(item.getSpecJson());
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(item.getUnitPrice());
            BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            Integer quantity = item.getQuantity() != null ? item.getQuantity() : 0;
            detail.setSubTotal(unitPrice.multiply(new BigDecimal(quantity)).setScale(2, RoundingMode.HALF_UP));
            detail.setCreateTime(LocalDateTime.now());
            details.add(detail);
        }

        // 批量插入明细
        for (OrderDetail detail : details) {
            orderDetailMapper.insert(detail);
        }
        log.info("Order details inserted: orderNo={}, detailCount={}", orderNo, details.size());

        // 9. 删除已下单的购物车项（硬删除）
        if (!CollectionUtils.isEmpty(dto.getCartItemIds())) {
            orderCartMapper.deleteBatchIds(dto.getCartItemIds());
        } else {
            LambdaQueryWrapper<OrderCart> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(OrderCart::getUserId, userId);
            orderCartMapper.delete(deleteWrapper);
        }
        log.info("Cart items deleted for userId={}", userId);

        // 10. 发送订单创建消息
        try {
            OrderMessage message = OrderMessage.builder()
                    .orderId(orderMain.getId())
                    .orderNo(orderNo)
                    .userId(userId)
                    .payAmount(payAmount)
                    .status(orderMain.getStatus())
                    .timestamp(LocalDateTime.now())
                    .build();
            orderStatusProducer.sendOrderCreated(message);
        } catch (Exception e) {
            log.error("Failed to send order created message: orderNo={}", orderNo, e);
            // 不抛出异常，订单已创建成功，消息通知失败不影响核心流程
        }

        return new CreateOrderResult(orderMain.getId(), orderNo, payAmount);
    }

    @Override
    public Page<OrderMain> pageByUserId(Long userId, Integer status, int page, int size) {
        LambdaQueryWrapper<OrderMain> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderMain::getUserId, userId);
        if (status != null) {
            wrapper.eq(OrderMain::getStatus, status);
        }
        wrapper.orderByDesc(OrderMain::getCreateTime);

        return orderMainMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public OrderDetailDTO getOrderDetail(Long orderId, Long userId) {
        OrderMain order = orderMainMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!userId.equals(order.getUserId())) {
            throw new RuntimeException("无权查看此订单");
        }

        List<OrderDetail> items = queryOrderDetails(orderId);

        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setOrder(order);
        dto.setItems(items);
        dto.setPaymentStatus(getPaymentStatusDesc(order.getStatus()));
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId, String reason) {
        OrderMain order = orderMainMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!userId.equals(order.getUserId())) {
            throw new RuntimeException("无权取消此订单");
        }
        if (order.getStatus() != OrderMain.STATUS_PENDING
                && order.getStatus() != OrderMain.STATUS_CONFIRMED) {
            throw new RuntimeException("当前订单状态不允许取消");
        }

        OrderMain update = new OrderMain();
        update.setId(orderId);
        update.setStatus(OrderMain.STATUS_CANCELLED);
        update.setCancelReason(reason);
        update.setCancelTime(LocalDateTime.now());
        update.setUpdateTime(LocalDateTime.now());
        orderMainMapper.updateById(update);

        log.info("Order cancelled: orderId={}, userId={}, reason={}", orderId, userId, reason);

        sendStatusChangeMessage(order, OrderMain.STATUS_CANCELLED);
    }

    // ======================== 管理端接口 ========================

    @Override
    public Page<OrderMain> adminPageOrders(Integer status, String orderNo, int page, int size) {
        LambdaQueryWrapper<OrderMain> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(OrderMain::getStatus, status);
        }
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            wrapper.like(OrderMain::getOrderNo, orderNo.trim());
        }
        wrapper.orderByDesc(OrderMain::getCreateTime);

        return orderMainMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public OrderDetailDTO adminGetOrderDetail(Long orderId) {
        OrderMain order = orderMainMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        List<OrderDetail> items = queryOrderDetails(orderId);

        OrderDetailDTO dto = new OrderDetailDTO();
        dto.setOrder(order);
        dto.setItems(items);
        dto.setPaymentStatus(getPaymentStatusDesc(order.getStatus()));
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminChangeStatus(Long orderId, Integer newStatus, String cancelReason) {
        if (newStatus == null) {
            throw new RuntimeException("新状态不能为空");
        }

        OrderMain order = orderMainMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        int currentStatus = order.getStatus();

        // 验证状态流转
        if (currentStatus == OrderMain.STATUS_COMPLETED) {
            throw new RuntimeException("已完成的订单不可变更状态");
        }

        if (currentStatus == OrderMain.STATUS_CANCELLED) {
            throw new RuntimeException("已取消的订单不可变更状态");
        }

        boolean validTransition = false;

        if (newStatus == OrderMain.STATUS_CANCELLED) {
            // 任何非完成状态的订单都可以取消
            validTransition = true;
        } else if (currentStatus == OrderMain.STATUS_PENDING
                && newStatus == OrderMain.STATUS_CONFIRMED) {
            validTransition = true;
        } else if (currentStatus == OrderMain.STATUS_CONFIRMED
                && newStatus == OrderMain.STATUS_PREPARING) {
            validTransition = true;
        } else if (currentStatus == OrderMain.STATUS_PREPARING
                && newStatus == OrderMain.STATUS_COMPLETED) {
            validTransition = true;
        }

        if (!validTransition) {
            throw new RuntimeException(String.format(
                    "无效的状态流转: %d -> %d", currentStatus, newStatus));
        }

        // 构建更新对象
        OrderMain update = new OrderMain();
        update.setId(orderId);
        update.setStatus(newStatus);
        update.setUpdateTime(LocalDateTime.now());

        // 根据新状态设置对应的时间字段
        if (newStatus == OrderMain.STATUS_CONFIRMED) {
            update.setConfirmTime(LocalDateTime.now());
        } else if (newStatus == OrderMain.STATUS_COMPLETED) {
            update.setCompleteTime(LocalDateTime.now());
        } else if (newStatus == OrderMain.STATUS_CANCELLED) {
            update.setCancelReason(cancelReason);
            update.setCancelTime(LocalDateTime.now());
        }

        orderMainMapper.updateById(update);
        log.info("Order status changed: orderId={}, {} -> {}", orderId, currentStatus, newStatus);

        sendStatusChangeMessage(order, newStatus);
    }

    // ======================== 私有辅助方法 ========================

    /**
     * 生成订单号：yyyyMMddHHmmss + 6位随机数字
     */
    private String generateOrderNo() {
        String dateTimePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomPart = 100000 + new Random().nextInt(900000);
        return dateTimePart + randomPart;
    }

    /**
     * 查询订单明细
     */
    private List<OrderDetail> queryOrderDetails(Long orderId) {
        LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderDetail::getOrderId, orderId);
        return orderDetailMapper.selectList(wrapper);
    }

    /**
     * 获取支付状态描述
     */
    private String getPaymentStatusDesc(int status) {
        switch (status) {
            case OrderMain.STATUS_PENDING:
                return "待支付";
            case OrderMain.STATUS_CONFIRMED:
                return "已支付";
            case OrderMain.STATUS_PREPARING:
                return "准备中";
            case OrderMain.STATUS_COMPLETED:
                return "已完成";
            case OrderMain.STATUS_CANCELLED:
                return "已取消";
            default:
                return "未知";
        }
    }

    /**
     * 发送订单状态变更消息
     */
    private void sendStatusChangeMessage(OrderMain order, Integer newStatus) {
        try {
            OrderMessage message = OrderMessage.builder()
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .userId(order.getUserId())
                    .payAmount(order.getPayAmount())
                    .status(newStatus)
                    .timestamp(LocalDateTime.now())
                    .build();
            orderStatusProducer.sendOrderStatusChanged(message);
        } catch (Exception e) {
            log.error("Failed to send order status change message: orderNo={}, newStatus={}",
                    order.getOrderNo(), newStatus, e);
            // 不抛出异常，状态已更新成功，消息通知失败不影响核心流程
        }
    }
}
