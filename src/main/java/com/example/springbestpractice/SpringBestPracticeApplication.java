package com.example.springbestpractice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SpringBestPracticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBestPracticeApplication.class, args);
    }

}
