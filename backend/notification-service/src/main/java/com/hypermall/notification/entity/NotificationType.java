package com.hypermall.notification.entity;

public enum NotificationType {
    // Order notifications
    ORDER_CREATED,
    ORDER_CONFIRMED,
    ORDER_PROCESSING,
    ORDER_SHIPPED,
    ORDER_DELIVERED,
    ORDER_CANCELLED,
    ORDER_RETURNED,

    // Payment notifications
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    REFUND_PROCESSED,

    // Promotion notifications
    PROMOTION,
    FLASH_SALE,
    PRICE_DROP,
    VOUCHER_EXPIRING,

    // Review notifications
    REVIEW_REPLY,
    REVIEW_LIKED,

    // Account notifications
    WELCOME,
    PASSWORD_RESET,
    EMAIL_VERIFICATION,
    ACCOUNT_SUSPENDED,

    // Seller notifications
    NEW_ORDER,
    LOW_STOCK,
    OUT_OF_STOCK,

    // System notifications
    SYSTEM_ANNOUNCEMENT,
    MAINTENANCE
}
