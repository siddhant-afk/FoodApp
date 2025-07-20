package com.foodapp.FoodApp.email_notifications.services;

import com.foodapp.FoodApp.email_notifications.dtos.NotificationDTO;

public interface NotificationService {

    void sendEmail(NotificationDTO notificationDTO);
}
