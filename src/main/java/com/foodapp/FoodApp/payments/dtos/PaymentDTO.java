package com.foodapp.FoodApp.payments.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.foodapp.FoodApp.auth_users.dtos.UserDTO;
import com.foodapp.FoodApp.auth_users.entities.User;
import com.foodapp.FoodApp.enums.PaymentGateway;
import com.foodapp.FoodApp.enums.PaymentStatus;
import com.foodapp.FoodApp.order.dtos.OrderDTO;
import com.foodapp.FoodApp.order.entities.Order;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentDTO {


    private Long id;

    private  Long orderId;


    private OrderDTO order;

    private BigDecimal amount;

    private PaymentStatus paymentStatus;

    private  String transactionId;


    private PaymentGateway paymentGateway;

    private String failureReason;

    private boolean success;

    private LocalDateTime paymentDate;

    private UserDTO user;
}
