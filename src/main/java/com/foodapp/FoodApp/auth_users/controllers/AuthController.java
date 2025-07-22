package com.foodapp.FoodApp.auth_users.controllers;


import com.foodapp.FoodApp.auth_users.dtos.LoginRequest;
import com.foodapp.FoodApp.auth_users.dtos.LoginResponse;
import com.foodapp.FoodApp.auth_users.dtos.RegistrationRequest;
import com.foodapp.FoodApp.auth_users.services.AuthService;
import com.foodapp.FoodApp.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {


    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Response<?>> register(@RequestBody @Valid RegistrationRequest registrationRequest){

        return ResponseEntity.ok(authService.register(registrationRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<Response<LoginResponse>> login(@RequestBody @Valid LoginRequest loginRequest){

        return ResponseEntity.ok(authService.login(loginRequest));
    }
}
