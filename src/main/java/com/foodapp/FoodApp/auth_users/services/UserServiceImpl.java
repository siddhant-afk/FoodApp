package com.foodapp.FoodApp.auth_users.services;

import com.foodapp.FoodApp.auth_users.dtos.UserDTO;
import com.foodapp.FoodApp.auth_users.entities.User;
import com.foodapp.FoodApp.auth_users.repositories.UserRepository;
import com.foodapp.FoodApp.aws.AWSS3Service;
import com.foodapp.FoodApp.email_notifications.dtos.NotificationDTO;
import com.foodapp.FoodApp.email_notifications.services.NotificationService;
import com.foodapp.FoodApp.exceptions.BadRequestException;
import com.foodapp.FoodApp.exceptions.NotFoundException;
import com.foodapp.FoodApp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;
    private final AWSS3Service awss3Service;
    @Override
    public User getCurrentLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public Response<List<UserDTO>> getAllUsers() {
        log.info("INSIDE getAllUsers()");

        List<User> userList = userRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));

        List<UserDTO> userDTOS = modelMapper.map(userList,new TypeToken<List<UserDTO>>(){
        }.getType());

        return Response.<List<UserDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("All users retrieved successfully")
                .data(userDTOS)
                .build();
    }

    @Override
    public Response<UserDTO> getOwnAccountDetails() {

        log.info("INSIDE getOwnAccountDetails()");
        User user = getCurrentLoggedInUser();

        UserDTO userDTO = modelMapper.map(user,UserDTO.class);

        return Response.<UserDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Success")
                .data(userDTO)
                .build();

    }

    @Override
    public Response<?> updateOwnAccount(UserDTO userDTO) {
        log.info("INSIDE updateOwnAccount()");

        User user = getCurrentLoggedInUser();
        String profileUrl = user.getProfileUrl();

        MultipartFile imageFile = userDTO.getImageFile();

        if(imageFile != null && !imageFile.isEmpty()) {

            if (profileUrl != null && !profileUrl.isEmpty()) {
                String keyName = profileUrl.substring(profileUrl.lastIndexOf("/") + 1);
                awss3Service.deleteFile("profile/" + keyName);
                log.info("Deleted old profile image of s3");
            }


            String imageName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
            URL newImageUrl = awss3Service.uploadFile("profile/" + imageName, imageFile);
            user.setProfileUrl(newImageUrl.toString());
        }

        if(userDTO.getName() != null) user.setName(userDTO.getName());
        if(userDTO.getPhoneNumber() != null) user.setPhoneNumber(userDTO.getPhoneNumber());
        if(userDTO.getAddress() != null) user.setAddress(userDTO.getAddress());
        if(userDTO.getPassword() != null) user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        if(userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())){

            if(userRepository.existsByEmail(userDTO.getEmail())){
                throw new BadRequestException("Email already exists");
            }

            user.setEmail(userDTO.getEmail());
        }

        userRepository.save(user);


        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account updated successfully")
                .build();

    }

    @Override
    public Response<?> deactivateOwnAccount() {
        log.info("INSIDE deactivateOwnAccount()");

        User user = getCurrentLoggedInUser();

        user.setActive(false);

        userRepository.save(user);

        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(user.getEmail())
                .subject("Account Deactivated")
                .body("Your account has been deactivated. If this was a mistake, please contact customer support")
                .build();

        notificationService.sendEmail(notificationDTO);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Account deactivated successfully")
                .build();
    }
}
