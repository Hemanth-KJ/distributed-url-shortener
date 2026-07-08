package com.hemanth.distributedurlshortener.service.impl;

import com.hemanth.distributedurlshortener.dto.request.CreateShortUrlRequest;
import com.hemanth.distributedurlshortener.dto.response.ShortUrlResponse;
import com.hemanth.distributedurlshortener.entity.Url;
import com.hemanth.distributedurlshortener.exception.ResourceNotFoundException;
import com.hemanth.distributedurlshortener.repository.UrlRepository;
import com.hemanth.distributedurlshortener.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private static final Logger logger =
            LoggerFactory.getLogger(UrlServiceImpl.class);

    private final UrlRepository urlRepository;

    @Override
    public ShortUrlResponse createShortUrl(CreateShortUrlRequest request) {

        logger.info("Received request to create short URL for: {}", request.getOriginalUrl());

        try {

            // Generate short code
            String shortCode = UUID.randomUUID()
                    .toString()
                    .substring(0, 8);

            logger.info("Generated short code: {}", shortCode);

            // Create URL entity
            Url url = Url.builder()
                    .originalUrl(request.getOriginalUrl())
                    .shortCode(shortCode)
                    .createdAt(LocalDateTime.now())
                    .clickCount(0L)
                    .build();

            logger.info("Saving URL to database...");

            // Save URL
            urlRepository.save(url);

            logger.info("URL saved successfully with ID: {}", url.getId());

            // Build response
            ShortUrlResponse response = ShortUrlResponse.builder()
                    .originalUrl(url.getOriginalUrl())
                    .shortCode(shortCode)
                    .shortUrl("http://localhost:8080/" + shortCode)
                    .build();

            logger.info("Short URL created successfully.");

            return response;

        } catch (Exception ex) {

            logger.error("Failed to create short URL.", ex);

            throw ex;
        }
    }

    @Override
    public String getOriginalUrl(String shortCode) {

        logger.info("Searching for short code: {}", shortCode);

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    logger.warn("Short URL not found: {}", shortCode);
                    return new ResourceNotFoundException(
                            "Short URL not found: " + shortCode);
                });

        logger.info("Short URL found. Original URL: {}", url.getOriginalUrl());

        // Increase click count
        url.setClickCount(url.getClickCount() + 1);

        logger.info("Click count updated to {}", url.getClickCount());

        // Save updated click count
        urlRepository.save(url);

        logger.info("Updated click count saved successfully.");

        return url.getOriginalUrl();
    }
}