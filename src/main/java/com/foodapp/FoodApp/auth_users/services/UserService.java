package com.foodapp.FoodApp.auth_users.services;

import com.foodapp.FoodApp.auth_users.dtos.UserDTO;
import com.foodapp.FoodApp.auth_users.entities.User;
import com.foodapp.FoodApp.response.Response;

import java.util.List;

public interface UserService {

    User getCurrentLoggedInUser();

    Response<List<UserDTO>> getAllUsers();

    Response<UserDTO> getOwnAccountDetails();

    Response<?> updateOwnAccount(UserDTO userDTO);

    Response<?> deactivateOwnAccount();

}


