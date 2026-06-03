package org.example.tnal_prochum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class TnalProchumApplication {

    @PostConstruct
    public void init() {
        // Set timezone to Cambodia
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Phnom_Penh"));
    }

    public static void main(String[] args) {
        SpringApplication.run(TnalProchumApplication.class, args);
    }
}