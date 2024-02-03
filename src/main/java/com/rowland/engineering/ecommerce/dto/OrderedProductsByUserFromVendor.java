package com.rowland.engineering.ecommerce.dto;

import com.rowland.engineering.ecommerce.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;




@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderedProductsByUserFromVendor {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String alternativePhoneNumber;
    private String deliveryAddress;
    private String additionalInformation;
    private  String region;
    private String state;

    private LocalDateTime purchaseDate;


    private double total;
    private int quantity;
    private List<OrderedProductsByUserFromVendor.CartItem> cart;

    private PaymentStatus paymentStatus;

    private String paymentReference;


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
