package com.smartbus.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookingApplication.class, args);
		System.out.println("=============================================");
		System.out.println("   🚀 SMART BUS BOOKING BACKEND IS READY!     ");
		System.out.println("   📡 REST API running at http://localhost:8080/api");
		System.out.println("=============================================");
	}

}
