package com.foodapp.FoodApp.cart.repositories;


import com.foodapp.FoodApp.cart.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem,Long> {


    
}
