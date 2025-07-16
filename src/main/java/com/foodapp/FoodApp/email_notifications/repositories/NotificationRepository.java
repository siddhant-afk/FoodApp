package com.foodapp.FoodApp.email_notifications.repositories;

import com.foodapp.FoodApp.email_notifications.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository  extends JpaRepository<Notification,Long> {


}
