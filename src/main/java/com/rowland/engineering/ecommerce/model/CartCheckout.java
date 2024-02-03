package com.rowland.engineering.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.List;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "cart_checked_out")
public class CartCheckout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String alternativePhoneNumber;
    private String deliveryAddress;
    private String additionalInformation;
    private  String region;
    private String state;
    private double price;
    private int quantity;
    private Long userId;

    @Column(name = "payment_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_reference")
    private String paymentReference;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime purchaseDate;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "cart_checked_out_id")
    private List<CartItem> cart;


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Entity
    @Table(name = "cart_item")
    public static class CartItem {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private Long productId;
        private String productName;
        private double price;
        private int quantity;
        private String imageUrl;
        private double subtotal;

        @NaturalId
        @Column(name = "cart_checked_out_id")
        private Long cartCheckoutId;

    }
}
