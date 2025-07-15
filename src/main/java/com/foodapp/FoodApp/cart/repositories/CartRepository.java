package com.foodapp.FoodApp.cart.repositories;

import com.foodapp.FoodApp.cart.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart,Long> {

    Optional<Cart> findByUser_Id(Long userId);

}
