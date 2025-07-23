package com.foodapp.FoodApp.auth_users.controllers;


import com.foodapp.FoodApp.auth_users.dtos.UserDTO;
import com.foodapp.FoodApp.auth_users.services.UserService;
import com.foodapp.FoodApp.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<List<UserDTO>>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());


    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    public ResponseEntity<Response<?>> updateOwnAccount(
            @ModelAttribute @Valid UserDTO userDTO,
            @RequestPart(value = "imageFile", required = false)MultipartFile imageFile
            ){

        userDTO.setImageFile(imageFile);
        return ResponseEntity.ok(userService.updateOwnAccount(userDTO));
    }


    @DeleteMapping("/deactivate")
    public ResponseEntity<Response<?>> deactivateOwnAccount(){
        return ResponseEntity.ok(userService.deactivateOwnAccount());
    }


    @GetMapping("/account")
    public ResponseEntity<Response<UserDTO>> getOwnAccountDetails(){

        return  ResponseEntity.ok(userService.getOwnAccountDetails());
    }
}
