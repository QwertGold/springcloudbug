package com.example.bugreport;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BugreportApplication {

    public static void main(String[] args) {
        SpringApplication.run(BugreportApplication.class, args);
    }

}
