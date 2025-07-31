package com.foodapp.FoodApp.review.services;

import com.foodapp.FoodApp.response.Response;
import com.foodapp.FoodApp.review.dtos.ReviewDTO;

import java.util.List;

public interface ReviewService {

    Response<ReviewDTO> createReview(ReviewDTO reviewDTO);
    Response<List<ReviewDTO>> getReviewsForMenu(Long menuId);

    Response<Double> getAverageRating(Long menuId);
}
