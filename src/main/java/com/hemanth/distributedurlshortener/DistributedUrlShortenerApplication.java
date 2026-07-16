package com.hemanth.distributedurlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
@SpringBootApplication
@EnableKafka
public class DistributedUrlShortenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributedUrlShortenerApplication.class, args);
    }

}
