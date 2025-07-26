package com.foodapp.FoodApp.cart.services;

import com.foodapp.FoodApp.auth_users.entities.User;
import com.foodapp.FoodApp.auth_users.services.UserService;
import com.foodapp.FoodApp.cart.dtos.CartDTO;
import com.foodapp.FoodApp.cart.entities.Cart;
import com.foodapp.FoodApp.cart.entities.CartItem;
import com.foodapp.FoodApp.cart.repositories.CartItemRepository;
import com.foodapp.FoodApp.cart.repositories.CartRepository;
import com.foodapp.FoodApp.exceptions.NotFoundException;
import com.foodapp.FoodApp.menu.entities.Menu;
import com.foodapp.FoodApp.menu.repositories.MenuRepository;
import com.foodapp.FoodApp.response.Response;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartServiceImpl implements  CartService{


    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final MenuRepository menuRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    public Response<?> addItemToCart(CartDTO cartDTO) {
        log.info("Insider addItemToCart()");

        Long menuId = cartDTO.getMenuId();
        int quantity = cartDTO.getQuantity();

        User user = userService.getCurrentLoggedInUser();

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new NotFoundException("Menu Item not found."));


        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setCartItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });

        // Check if the item is already in cart.
        Optional<CartItem> optionalCartItem = cart.getCartItems().stream()
                .filter(cartItem -> cartItem.getMenu().getId().equals(menuId))
                .findFirst();

        // If present, increment item.

        if(optionalCartItem.isPresent()){
            CartItem cartItem = optionalCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setSubTotal(cartItem.getPricePerUnit().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            cartItemRepository.save(cartItem);
        }else{

            CartItem newCartItem = CartItem.builder()
                    .cart(cart)
                    .menu(menu)
                    .quantity(quantity)
                    .pricePerUnit(menu.getPrice())
                    .subTotal(menu.getPrice().multiply(BigDecimal.valueOf(quantity)))
                    .build();

            cart.getCartItems().add(newCartItem);
            cartItemRepository.save(newCartItem);
        }

        cartRepository.save(cart);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item added to cart successfully")
                .build();

    }

    @Override
    public Response<?> incrementItem(Long menuId) {
        log.info("Inside incrementItem()");

        User user = userService.getCurrentLoggedInUser();

        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found."));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getMenu().getId().equals(menuId))
                .findFirst().orElseThrow(() -> new NotFoundException("Menu not found in cart."));

        int newQuantity = cartItem.getQuantity() + 1;
        cartItem.setQuantity(newQuantity);
        cartItem.setSubTotal(cartItem.getPricePerUnit().multiply(BigDecimal.valueOf(newQuantity)));

        cartItemRepository.save(cartItem);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item quantity incremented Successfully")
                .build();

    }

    @Override
    public Response<?> decrementItem(Long menuId) {
        log.info("Inside decrementItem()");

        User user = userService.getCurrentLoggedInUser();

        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found."));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getMenu().getId().equals(menuId))
                .findFirst().orElseThrow(() -> new NotFoundException("Menu not found in cart."));

        int newQuantity = cartItem.getQuantity() - 1;

        if(newQuantity > 0){
            cartItem.setQuantity(newQuantity);
            cartItem.setSubTotal(cartItem.getPricePerUnit().multiply(BigDecimal.valueOf(newQuantity)));

            cartItemRepository.save(cartItem);
        }else{
            cart.getCartItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        }

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item quantity decremented Successfully")
                .build();

    }

    @Override
    public Response<?> removeItem(Long cartItemId) {
        log.info("Inside removeItem()");

        User user = userService.getCurrentLoggedInUser();

        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found."));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("Cart Item not found"));

        if(!cart.getCartItems().contains(cartItem)){
            throw new NotFoundException("Cart item does not belong to this user's cart.");
        }

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Item removed from cart successfully")
                .build();

    }

    @Override
    @Transactional(readOnly = true)
    public Response<CartDTO> getShoppingCart() {
        log.info("Inside getShoppingCart()");

        User user = userService.getCurrentLoggedInUser();

        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found for user"));

        List<CartItem> cartItems = cart.getCartItems();

        CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);

        // Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        if(cartItems != null){
            for (CartItem item : cartItems){
                totalAmount = totalAmount.add(item.getSubTotal());
            }
        }

        cartDTO.setTotalAmount(totalAmount);

        // remove the review from the response
        if(cartDTO.getCartItems() != null){
            cartDTO.getCartItems().forEach(item -> item.getMenu().setReviews(null));
        }

        return Response.<CartDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Shopping cart retrieved successfully.")
                .data(cartDTO)
                .build();

    }

    @Override
    public Response<?> clearShoppingCart() {
        log.info("Inside clearShoppingCart()");

        User user = userService.getCurrentLoggedInUser();

        Cart cart = cartRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found for user."));

        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();

        cartRepository.save(cart);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Shopping Cart cleared successfully")
                .build();

    }
}
