package com.hemanth.distributedurlshortener.kafka;

import com.hemanth.distributedurlshortener.entity.Url;
import com.hemanth.distributedurlshortener.exception.ResourceNotFoundException;
import com.hemanth.distributedurlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UrlClickConsumer {

    private static final Logger logger =
            LoggerFactory.getLogger(UrlClickConsumer.class);

    private final UrlRepository urlRepository;

    @KafkaListener(
            topics = "url-clicks",
            groupId = "url-shortener-group"
    )
    public void consumeClick(String shortCode) {

        try {

            logger.info("Received click event for {}", shortCode);

            Url url = urlRepository.findByShortCode(shortCode)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Short URL not found"));

            url.setClickCount(url.getClickCount() + 1);

            urlRepository.save(url);

            logger.info("Updated click count for {} -> {}",
                    shortCode,
                    url.getClickCount());

        } catch (Exception ex) {

            logger.error("Failed to process click event for {}",
                    shortCode,
                    ex);
        }
    }
}