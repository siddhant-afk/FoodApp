package com.foodapp.FoodApp.auth_users.repositories;

import com.foodapp.FoodApp.auth_users.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

}
