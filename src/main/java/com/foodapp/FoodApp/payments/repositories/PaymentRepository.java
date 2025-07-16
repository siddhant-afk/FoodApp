package com.foodapp.FoodApp.payments.repositories;

import com.foodapp.FoodApp.payments.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
}
