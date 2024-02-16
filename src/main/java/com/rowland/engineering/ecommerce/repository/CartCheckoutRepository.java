package com.rowland.engineering.ecommerce.repository;

import com.rowland.engineering.ecommerce.model.CartCheckout;
import com.rowland.engineering.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;





@Repository
public interface CartCheckoutRepository extends JpaRepository<CartCheckout, Long> {

    List<CartCheckout> findByUserId(Long userId);
    @Query("SELECT c FROM CartCheckout$CartItem c WHERE c.productId IN :productIds")
    List<CartCheckout.CartItem> findCartItemsByProductIds(@Param("productIds") List<Long> productIds);

//    @Query("SELECT c FROM `CartCheckout$CartItem` c WHERE c.productId IN :productIds")
//    List<CartCheckout.CartItem> findCartItemsByProductIds(@Param("productIds") List<Long> productIds);

}

