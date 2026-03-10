package com.hypermall.shipping.entity;

public enum ShippingProvider {
    GHN("Giao Hang Nhanh"),
    GHTK("Giao Hang Tiet Kiem"),
    VIETTEL_POST("Viettel Post"),
    JT_EXPRESS("J&T Express"),
    NINJA_VAN("Ninja Van"),
    BEST_EXPRESS("Best Express");

    private final String displayName;

    ShippingProvider(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
