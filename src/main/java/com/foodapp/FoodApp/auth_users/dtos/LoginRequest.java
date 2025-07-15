package com.foodapp.FoodApp.auth_users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {


    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid Email Address.")
    private String email;

    @NotBlank(message = "Password is required.")
    private String password;
}
