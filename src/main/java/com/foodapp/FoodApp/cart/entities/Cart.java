package com.foodapp.FoodApp.cart.entities;


import com.foodapp.FoodApp.auth_users.entities.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name ="carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;


    @OneToMany(mappedBy = "cart" , cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems;

    private String promoCode;
}
