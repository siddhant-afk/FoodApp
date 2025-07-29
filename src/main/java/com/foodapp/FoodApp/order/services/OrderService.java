package com.foodapp.FoodApp.order.services;

import com.foodapp.FoodApp.enums.OrderStatus;
import com.foodapp.FoodApp.order.dtos.OrderDTO;
import com.foodapp.FoodApp.order.dtos.OrderItemDTO;
import com.foodapp.FoodApp.order.entities.Order;
import com.foodapp.FoodApp.response.Response;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {

    Response<?> placeOrderFromCart();
    Response<OrderDTO> getOrderById(Long id);
    Response<Page<OrderDTO>> getAllOrders(OrderStatus orderStatus, int page, int size);
    Response<List<OrderDTO>> getOrdersOfUser();
    Response<OrderItemDTO> getOrderItemById(Long orderItemId);
    Response<OrderDTO> updateOrderStatus(OrderDTO orderDTO);
    Response<Long> countUniqueCustomers();
}
