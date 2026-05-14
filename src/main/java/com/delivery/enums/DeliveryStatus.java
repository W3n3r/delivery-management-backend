package com.delivery.enums;

/**
 * Delivery status for tracking order lifecycle.
 * PENDING: Order created, awaiting assignment
 * ASSIGNED: Order assigned to a shipper
 * PICKED_UP: Shipper picked up the package
 * DELIVERED: Order successfully delivered
 * CANCELLED: Order cancelled
 */
public enum DeliveryStatus {
    PENDING,
    ASSIGNED,
    PICKED_UP,
    DELIVERED,
    CANCELLED
}
