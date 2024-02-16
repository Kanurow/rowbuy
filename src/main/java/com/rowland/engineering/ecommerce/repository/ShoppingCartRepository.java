package com.rowland.engineering.ecommerce.repository;

import com.rowland.engineering.ecommerce.model.Product;
import com.rowland.engineering.ecommerce.model.ShoppingCart;
import com.rowland.engineering.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    List<ShoppingCart> findAllByUserId(Long userId);


    Optional<ShoppingCart> findByProductAndUser(Product product, User user);

    void deleteByProductId(Long id);

    ShoppingCart findByProductIdAndUserId(Long id, Long id1);
}
