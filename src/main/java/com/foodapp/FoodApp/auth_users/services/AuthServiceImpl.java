package com.foodapp.FoodApp.auth_users.services;


import com.foodapp.FoodApp.auth_users.dtos.LoginRequest;
import com.foodapp.FoodApp.auth_users.dtos.LoginResponse;
import com.foodapp.FoodApp.auth_users.dtos.RegistrationRequest;
import com.foodapp.FoodApp.auth_users.entities.User;
import com.foodapp.FoodApp.auth_users.repositories.UserRepository;
import com.foodapp.FoodApp.exceptions.BadRequestException;
import com.foodapp.FoodApp.exceptions.NotFoundException;
import com.foodapp.FoodApp.response.Response;
import com.foodapp.FoodApp.role.entities.Role;
import com.foodapp.FoodApp.role.repositories.RoleRepository;
import com.foodapp.FoodApp.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RoleRepository roleRepository;


    @Override
    public Response<?> register(RegistrationRequest registrationRequest) {
        log.info("INSIDE register()");

        if(userRepository.existsByEmail(registrationRequest.getEmail())){
          throw new BadRequestException("Email already exists");
        }

        List<Role> userRoles;

        if(registrationRequest.getRoles() != null && !registrationRequest.getRoles().isEmpty()){

            userRoles =  registrationRequest.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName.toUpperCase())
                            .orElseThrow(() -> new NotFoundException("Role with name: "+roleName + "Not Found")))
                    .toList();

        }else{
            Role defaultRole = roleRepository.findByName("CUSTOMER")
                    .orElseThrow(() -> new NotFoundException("Default CUSTOMER role not found"));

            userRoles = List.of(defaultRole);
        }

        User userToSave = User.builder()
                .name(registrationRequest.getName())
                .email(registrationRequest.getEmail())
                .address(registrationRequest.getAddress())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .phoneNumber(registrationRequest.getPhoneNumber())
                .roles(userRoles)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(userToSave);

        log.info("User registered successfully");

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("User Registered Successfully")
                .build();



    }

    @Override
    public Response<LoginResponse> login(LoginRequest loginRequest) {

        log.info("INSIDE login()");

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("Invalid Email"));

        if(!user.isActive()){
            throw new NotFoundException("Account not active. Please contact customer support");
        }

        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new BadRequestException("Invalid Password");
        }

        String token = jwtUtils.generateToken(user.getEmail());

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setRoles(roleNames);


        return Response.<LoginResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Login Successful")
                .data(loginResponse)
                .build();
    }
}
