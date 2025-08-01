package com.foodapp.FoodApp.payments.services;


import com.foodapp.FoodApp.email_notifications.dtos.NotificationDTO;
import com.foodapp.FoodApp.email_notifications.services.NotificationService;
import com.foodapp.FoodApp.enums.OrderStatus;
import com.foodapp.FoodApp.enums.PaymentGateway;
import com.foodapp.FoodApp.enums.PaymentStatus;
import com.foodapp.FoodApp.exceptions.BadRequestException;
import com.foodapp.FoodApp.exceptions.NotFoundException;
import com.foodapp.FoodApp.order.entities.Order;
import com.foodapp.FoodApp.order.repositories.OrderRepository;
import com.foodapp.FoodApp.payments.dtos.PaymentDTO;
import com.foodapp.FoodApp.payments.entities.Payment;
import com.foodapp.FoodApp.payments.repositories.PaymentRepository;
import com.foodapp.FoodApp.response.Response;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final OrderRepository orderRepository;
    private final TemplateEngine templateEngine;
    private final ModelMapper modelMapper;

    @Value("${stripe.api.secret.key}")
    private String secretKey;

    @Value("${frontend.base.url}")
    private String frontendBaseUrl;

    @Override
    public Response<?> initializePayment(PaymentDTO paymentRequest) {
        log.info("Insider initializePayment()");
        Stripe.apiKey = secretKey;

        Long orderId = paymentRequest.getOrderId();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order Not Found."));

        if(order.getPaymentStatus() == PaymentStatus.COMPLETED){
            throw new BadRequestException("Payment already made for this order");
        }

        if(paymentRequest.getAmount() == null){
            throw new BadRequestException("Amont you are passing in is null");
        }

        if(order.getTotalAmount().compareTo(paymentRequest.getAmount()) != 0){
            throw new BadRequestException("Payment amount does not tally. Please contact our customer support agent.");
        }

        try{
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(paymentRequest.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
                    .setCurrency("usd")
                    .putMetadata("orderId", String.valueOf(orderId))
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            String uniqueTransactionId = intent.getClientSecret();

            return Response.builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("success")
                    .data(uniqueTransactionId)
                    .build();

        } catch (Exception e){
            throw new RuntimeException("Error creating payment unique transaction id");
        }
    }

    @Override
    public void updatePaymentForOrder(PaymentDTO paymentDTO) {
        log.info("Insider updatePaymentForOrder()");

        Long orderId = paymentDTO.getOrderId();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        Payment payment = new Payment();
        payment.setPaymentGateway(PaymentGateway.STRIPE);
        payment.setAmount(paymentDTO.getAmount());
        payment.setTransactionId(paymentDTO.getTransactionId());
        payment.setPaymentStatus(paymentDTO.isSuccess() ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setOrder(order);

        if(!paymentDTO.isSuccess()){
            payment.setFailureReason(paymentDTO.getFailureReason());
        }

        paymentRepository.save(payment);

        // Email context
        Context context = new Context(Locale.getDefault());
        context.setVariable("customerName",order.getUser().getName());
        context.setVariable("orderId",order.getId());
        context.setVariable("currentYear", Year.now().getValue());
        context.setVariable("amount","$" + paymentDTO.getAmount());

        if(paymentDTO.isSuccess()){
            order.setPaymentStatus(PaymentStatus.COMPLETED);
            order.setOrderStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);

            context.setVariable("transactionId", paymentDTO.getTransactionId());
            context.setVariable("paymentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")));
            context.setVariable("frontendBaseUrl",this.frontendBaseUrl);

            String emailBody = templateEngine.process("payment-success",context);

            notificationService.sendEmail(
                    NotificationDTO.builder()
                            .recipient(order.getUser().getEmail())
                            .subject("Payment Successful - Order #" + order.getId())
                            .body(emailBody)
                            .isHtml(true)
                            .build()
            );
        }
        else{
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            context.setVariable("failureReason",paymentDTO.getFailureReason());

            String emailBody = templateEngine.process("payment-failed",context);

            notificationService.sendEmail(
                    NotificationDTO.builder()
                            .recipient(order.getUser().getEmail())
                            .subject("Payment Failed - Order #" + order.getId())
                            .body(emailBody)
                            .isHtml(true)
                            .build());
        }
    }

    @Override
    public Response<List<PaymentDTO>> getAllPayments() {
        log.info("Insider getAllPayments()");

        List<Payment> paymentList = paymentRepository.findAll((Sort.by(Sort.Direction.DESC,"id")));

        List<PaymentDTO> paymentDTOS = modelMapper.map(paymentList, new TypeToken<List<PaymentDTO>>(){}.getType());

        paymentDTOS.forEach(item -> {
            item.setOrder(null);
            item.setUser(null);
        });

        return Response.<List<PaymentDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Payments retrieved successfully")
                .data(paymentDTOS)
                .build();
    }

    @Override
    public Response<PaymentDTO> getPaymentById(Long paymentId) {
        log.info("Insider getPaymentById()");

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        PaymentDTO paymentDTO = modelMapper.map(payment,PaymentDTO.class);

        paymentDTO.getUser().setRoles(null);
        payment.getOrder().setUser(null);
        paymentDTO.getOrder().getOrderItems().forEach(item ->{
            item.getMenu().setReviews(null);
        });


        return Response.<PaymentDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Payment retrieved successfulyy by id")
                .data(paymentDTO)
                .build();
    }
}
