package com.foodapp.FoodApp.auth_users.dtos;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodapp.FoodApp.role.dtos.RoleDTO;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {

    private Long id;

    private String email;
    private String name;
    private String phoneNumber;

    private String profileUrl;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private boolean isActive;

    private String address;
    private List<RoleDTO> roles;

    private MultipartFile imageFile;
}
