package com.restaurant.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_main")
public class OrderMain {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 订单号（yyyyMMddHHmmss + 6位随机数） */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 地址JSON（TEXT字段，存储收货人、电话、地址等完整信息） */
    private String addressJson;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 优惠金额 */
    private BigDecimal discountAmount;

    /** 实付金额 */
    private BigDecimal payAmount;

    /**
     * 订单状态：
     * 0 - PENDING（待确认）
     * 1 - CONFIRMED（已确认）
     * 2 - PREPARING（准备中）
     * 3 - COMPLETED（已完成）
     * 4 - CANCELLED（已取消）
     */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 取消原因 */
    private String cancelReason;

    /** 确认时间 */
    private LocalDateTime confirmTime;

    /** 完成时间 */
    private LocalDateTime completeTime;

    /** 取消时间 */
    private LocalDateTime cancelTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    // ====== 状态常量 ======
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_CONFIRMED = 1;
    public static final int STATUS_PREPARING = 2;
    public static final int STATUS_COMPLETED = 3;
    public static final int STATUS_CANCELLED = 4;
}
