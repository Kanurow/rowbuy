package com.rowland.engineering.ecommerce.dto;

import com.rowland.engineering.ecommerce.model.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartCheckoutRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String alternativePhoneNumber;
    private String deliveryAddress;
    private String additionalInformation;
    private  String region;
    private String state;

    private double total;
    private int quantity;
    private Long userId;

    private String paystackApproved;

    private String paystackReference;

    private List<CartItem> cart;


    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CartItem {
        private Long productId;
        private String productName;
        private double price;
        private String imageUrl;
        private int quantity;
        private double subtotal;

    }
}

