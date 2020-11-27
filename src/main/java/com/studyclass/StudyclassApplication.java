package com.studyclass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(proxyBeanMethods = false)
public class StudyclassApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyclassApplication.class, args);
    }

}
