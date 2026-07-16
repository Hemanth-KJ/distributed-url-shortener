package com.hemanth.distributedurlshortener.kafka;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UrlClickProducer {

    private static final Logger logger =
            LoggerFactory.getLogger(UrlClickProducer.class);

    private static final String TOPIC = "url-clicks";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publishClick(String shortCode) {

        kafkaTemplate.send(TOPIC, shortCode);

        logger.info("Published click event for {}", shortCode);
    }
}