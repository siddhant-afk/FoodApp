package com.foodapp.FoodApp.category.repositories;

import com.foodapp.FoodApp.category.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository  extends JpaRepository<Category,Long> {

    
}
