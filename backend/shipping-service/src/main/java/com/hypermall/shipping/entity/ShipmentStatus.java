package com.hypermall.shipping.entity;

public enum ShipmentStatus {
    PENDING,          // Waiting to be picked up
    PICKED_UP,        // Picked up from seller
    IN_TRANSIT,       // On the way
    OUT_FOR_DELIVERY, // Out for delivery
    DELIVERED,        // Successfully delivered
    FAILED_ATTEMPT,   // Delivery attempt failed
    RETURNED,         // Returned to sender
    CANCELLED         // Cancelled
}
