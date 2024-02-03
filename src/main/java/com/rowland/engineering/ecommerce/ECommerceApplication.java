package com.rowland.engineering.ecommerce;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.convert.Jsr310Converters;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.TimeZone;

@EnableTransactionManagement
@SpringBootApplication
@EntityScan(basePackageClasses = {
		ECommerceApplication.class,
		Jsr310Converters.class
})
public class ECommerceApplication {
	@PostConstruct
	void init(){
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	public static void main(String[] args) {
		SpringApplication.run(ECommerceApplication.class, args);
	}

}
