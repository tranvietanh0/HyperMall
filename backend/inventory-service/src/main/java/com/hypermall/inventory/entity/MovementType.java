package com.hypermall.inventory.entity;

public enum MovementType {
    IN,          // Stock in (restock, return)
    OUT,         // Stock out (sold, damaged)
    RESERVE,     // Reserved for order
    RELEASE,     // Released from reservation
    ADJUSTMENT   // Manual adjustment
}
