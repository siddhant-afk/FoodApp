package com.foodapp.FoodApp.payments.services;

import com.foodapp.FoodApp.payments.dtos.PaymentDTO;
import com.foodapp.FoodApp.response.Response;

import java.util.List;

public interface PaymentService {

    Response<?> initializePayment(PaymentDTO paymentRequest);
    void updatePaymentForOrder(PaymentDTO paymentDTO);
    Response<List<PaymentDTO>> getAllPayments();
    Response<PaymentDTO> getPaymentById(Long paymentId);
}
