package com.foodapp.FoodApp;


import com.foodapp.FoodApp.email_notifications.dtos.NotificationDTO;
import com.foodapp.FoodApp.email_notifications.services.NotificationService;

import com.foodapp.FoodApp.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;



@SpringBootApplication
@EnableAsync
//@RequiredArgsConstructor
public class FoodAppApplication {



	public static void main(String[] args) {
		SpringApplication.run(FoodAppApplication.class, args);
	}





//	@Bean
//	CommandLineRunner runner(){
//
//		return args -> {
//			NotificationDTO notificationDTO = NotificationDTO.builder()
//					.recipient("")
//					.subject("Hello Siddhant")
//					.body("This is a test email")
//					.type(NotificationType.EMAIL)
//					.build();
//
//			notificationService.sendEmail(notificationDTO);
//		};
//	}

}
