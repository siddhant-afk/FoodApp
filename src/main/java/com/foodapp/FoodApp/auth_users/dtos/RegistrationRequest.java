package com.foodapp.FoodApp.auth_users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RegistrationRequest {


    @NotBlank(message = "Name is required.")
    private String name;

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid Email Address.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 4, message = "Password must be at least 4 characters long")
    private String password;


    @NotBlank(message = "Address is required.")
    private String address;

    @NotBlank(message = "Phone number is required.")
    private String phoneNumber;

    private List<String> roles;

}
