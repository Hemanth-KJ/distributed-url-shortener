package com.hemanth.distributedurlshortener.service.impl;

import com.hemanth.distributedurlshortener.dto.request.CreateShortUrlRequest;
import com.hemanth.distributedurlshortener.dto.response.ShortUrlResponse;
import com.hemanth.distributedurlshortener.entity.Url;
import com.hemanth.distributedurlshortener.exception.ResourceNotFoundException;
import com.hemanth.distributedurlshortener.repository.UrlRepository;
import com.hemanth.distributedurlshortener.service.UrlService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private static final Logger logger =
            LoggerFactory.getLogger(UrlServiceImpl.class);

    private final UrlRepository urlRepository;

    @Override
    public ShortUrlResponse createShortUrl(CreateShortUrlRequest request) {

        logger.info("Received request to create short URL for: {}",
                request.getOriginalUrl());

        // Check if URL already exists
        Optional<Url> existingUrl =
                urlRepository.findByOriginalUrl(request.getOriginalUrl());

        if (existingUrl.isPresent()) {

            logger.info("URL already exists. Returning existing short URL.");

            return buildResponse(existingUrl.get());
        }

        // Generate unique short code
        String shortCode = generateUniqueShortCode();

        logger.info("Generated unique short code: {}", shortCode);

        // Create URL entity
        Url url = Url.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .createdAt(LocalDateTime.now())
                .clickCount(0L)
                .build();

        logger.info("Saving URL to database...");

        urlRepository.save(url);

        logger.info("URL saved successfully with ID: {}", url.getId());

        return buildResponse(url);
    }

    @Override
    public String getOriginalUrl(String shortCode) {

        logger.info("Searching for short code: {}", shortCode);

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    logger.error("Short URL not found: {}", shortCode);
                    return new ResourceNotFoundException(
                            "Short URL not found"
                    );
                });

        // Increment click count
        url.setClickCount(url.getClickCount() + 1);

        urlRepository.save(url);

        logger.info("Redirecting to: {}", url.getOriginalUrl());

        return url.getOriginalUrl();
    }

    @Override
    public void redirectToOriginalUrl(
            String shortCode,
            HttpServletResponse response)
            throws IOException {

        String originalUrl = getOriginalUrl(shortCode);

        response.sendRedirect(originalUrl);
    }

    /**
     * Generate a unique short code.
     */
    private String generateUniqueShortCode() {

        String shortCode;

        do {

            shortCode = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8);

        } while (urlRepository.findByShortCode(shortCode).isPresent());

        return shortCode;
    }

    /**
     * Build API response.
     */
    private ShortUrlResponse buildResponse(Url url) {

        return ShortUrlResponse.builder()
                .originalUrl(url.getOriginalUrl())
                .shortCode(url.getShortCode())
                .shortUrl("http://localhost:8080/" + url.getShortCode())
                .build();
    }
}