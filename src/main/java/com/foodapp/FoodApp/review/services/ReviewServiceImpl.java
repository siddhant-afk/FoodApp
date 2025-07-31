package com.foodapp.FoodApp.review.services;


import com.foodapp.FoodApp.auth_users.entities.User;
import com.foodapp.FoodApp.auth_users.services.UserService;
import com.foodapp.FoodApp.enums.OrderStatus;
import com.foodapp.FoodApp.exceptions.BadRequestException;
import com.foodapp.FoodApp.exceptions.NotFoundException;
import com.foodapp.FoodApp.menu.entities.Menu;
import com.foodapp.FoodApp.menu.repositories.MenuRepository;
import com.foodapp.FoodApp.order.entities.Order;
import com.foodapp.FoodApp.order.repositories.OrderItemRepository;
import com.foodapp.FoodApp.order.repositories.OrderRepository;
import com.foodapp.FoodApp.response.Response;
import com.foodapp.FoodApp.review.dtos.ReviewDTO;
import com.foodapp.FoodApp.review.entities.Review;
import com.foodapp.FoodApp.review.repositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService{


    private final ReviewRepository reviewRepository;
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ModelMapper modelMapper;
    private final UserService userService;

    @Override
    @Transactional
    public Response<ReviewDTO> createReview(ReviewDTO reviewDTO) {

        log.info("Inside createReview()");

        // Get current user
        User user = userService.getCurrentLoggedInUser();

        // Validate required fields
        if (reviewDTO.getOrderId() == null || reviewDTO.getMenuId() == null) {
            throw new BadRequestException("Order ID and Menu Item ID are required");
        }

        // Validate menu item exists
        Menu menu = menuRepository.findById(reviewDTO.getMenuId())
                .orElseThrow(() -> new NotFoundException("Menu item not found"));


        // Validate order exists
        Order order = orderRepository.findById(reviewDTO.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        //make sure the order belongs to you
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("This order doesn't belong to you");
        }

        // Validate order status is DELIVERED
        if (order.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("You can only review items that has been delivered to you");
        }

        // Validate that menu item was part of this order
        boolean itemInOrder = orderItemRepository.existsByOrderIdAndMenuId(
                reviewDTO.getOrderId(),
                reviewDTO.getMenuId());

        if (!itemInOrder) {
            throw new BadRequestException("This menu item was not part of the specified order");
        }

        // Check if user already wrote a review for the item
        if (reviewRepository.existsByUserIdAndMenuIdAndOrderId(
                user.getId(),
                reviewDTO.getMenuId(),
                reviewDTO.getOrderId())) {
            throw new BadRequestException("You've already reviewed this item from this order");
        }

        // Create and save review
        Review review = Review.builder()
                .user(user)
                .menu(menu)
                .orderId(reviewDTO.getOrderId())
                .rating(reviewDTO.getRating())
                .comment(reviewDTO.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);

        // Return response with review data
        ReviewDTO responseDto = modelMapper.map(savedReview, ReviewDTO.class);
        responseDto.setUsername(user.getName());
        responseDto.setMenuName(menu.getName());

        return Response.<ReviewDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Review added successfully")
                .data(responseDto)
                .build();

    }

    @Override
    public Response<List<ReviewDTO>> getReviewsForMenu(Long menuId) {
        log.info("Inside getReviewsForMenu()");

        List<Review> reviews = reviewRepository.findByMenuIdOrderByIdDesc(menuId);

        List<ReviewDTO> reviewDTOS = reviews.stream()
                .map((review -> modelMapper.map(review,ReviewDTO.class)))
                .toList();

        return Response.<List<ReviewDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Review retrieved successfully")
                .data(reviewDTOS)
                .build();
    }

    @Override
    public Response<Double> getAverageRating(Long menuId) {
        log.info("Inside getAverageRating()");

        Double averageRating = reviewRepository.calculateAverageRatingByMenuId(menuId);

        return Response.<Double>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Average rating retrieved successfully")
                .data(averageRating != null ? averageRating : 0.0)
                .build();
    }
}
