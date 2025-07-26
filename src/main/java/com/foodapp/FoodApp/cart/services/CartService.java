package com.foodapp.FoodApp.cart.services;

import com.foodapp.FoodApp.cart.dtos.CartDTO;
import com.foodapp.FoodApp.response.Response;

public interface CartService {

    Response<?> addItemToCart(CartDTO cartDTO);
    Response<?> incrementItem(Long menuId);
    Response<?> decrementItem(Long menuId);
    Response<?> removeItem(Long cartItemId);
    Response<CartDTO> getShoppingCart();
    Response<?> clearShoppingCart();
}
