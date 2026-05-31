package org.example.tnal_prochum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TnalProchumApplication {
    public static void main(String[] args) {
        SpringApplication.run(TnalProchumApplication.class, args);
    }
}