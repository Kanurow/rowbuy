package com.rowland.engineering.ecommerce.model;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    APPROVED("Approved"),
    FAILED("Failed");

    private final String value;
    PaymentStatus(String value) {
        this.value = value;
    }
}
