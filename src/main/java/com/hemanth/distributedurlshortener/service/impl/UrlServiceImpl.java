package com.hemanth.distributedurlshortener.service.impl;

import com.hemanth.distributedurlshortener.dto.request.CreateShortUrlRequest;
import com.hemanth.distributedurlshortener.dto.response.ShortUrlResponse;
import com.hemanth.distributedurlshortener.dto.response.UrlAnalyticsResponse;
import com.hemanth.distributedurlshortener.entity.Url;
import com.hemanth.distributedurlshortener.exception.ResourceNotFoundException;
import com.hemanth.distributedurlshortener.exception.UrlExpiredException;
import com.hemanth.distributedurlshortener.repository.UrlRepository;
import com.hemanth.distributedurlshortener.service.UrlService;
import com.hemanth.distributedurlshortener.exception.ShortCodeAlreadyExistsException;
import com.google.zxing.WriterException;
import com.hemanth.distributedurlshortener.util.QrCodeGenerator;
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

        // Check if original URL already exists
        Optional<Url> existingUrl =
                urlRepository.findByOriginalUrl(request.getOriginalUrl());

        if (existingUrl.isPresent()) {

            logger.info("URL already exists. Returning existing short URL.");

            return buildResponse(existingUrl.get());
        }

        String shortCode;

        // Use custom short code if provided
        if (request.getCustomShortCode() != null &&
                !request.getCustomShortCode().isBlank()) {

            shortCode = request.getCustomShortCode();

            logger.info("Custom short code requested: {}", shortCode);

            if (urlRepository.findByShortCode(shortCode).isPresent()) {

                logger.error("Custom short code already exists: {}", shortCode);

                throw new ShortCodeAlreadyExistsException("Short code already exists");
            }

        } else {

            // Generate random unique short code
            shortCode = generateUniqueShortCode();

            logger.info("Generated unique short code: {}", shortCode);
        }

        // Create entity
        Url url = Url.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .createdAt(LocalDateTime.now())
                .expiresAt(request.getExpiresAt())
                .clickCount(0L)
                .build();

        logger.info("Saving URL to database...");

        urlRepository.save(url);

        logger.info("URL saved successfully with ID: {}", url.getId());

        return buildResponse(url);
    }

    @Override
    public String getOriginalUrl(String shortCode) {

        logger.info("Looking up short code: {}", shortCode);

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    logger.error("Short code not found: {}", shortCode);
                    return new ResourceNotFoundException("Short URL not found");
                });

        // Check expiration
        if (url.getExpiresAt() != null &&
                LocalDateTime.now().isAfter(url.getExpiresAt())) {

            logger.warn("Short URL has expired: {}", shortCode);

            throw new UrlExpiredException("Short URL has expired");
        }

        // Increment click count
        url.setClickCount(url.getClickCount() + 1);

        urlRepository.save(url);

        logger.info("Redirecting to {}", url.getOriginalUrl());

        return url.getOriginalUrl();
    }

    @Override
    public UrlAnalyticsResponse getAnalytics(String shortCode) {

        logger.info("Fetching analytics for short code: {}", shortCode);

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    logger.error("Analytics requested for non-existing short code: {}", shortCode);
                    return new ResourceNotFoundException("Short URL not found");
                });

        logger.info("Analytics fetched successfully for short code: {}", shortCode);

        return UrlAnalyticsResponse.builder()
                .originalUrl(url.getOriginalUrl())
                .shortCode(url.getShortCode())
                .clickCount(url.getClickCount())
                .createdAt(url.getCreatedAt())
                .expiresAt(url.getExpiresAt())
                .build();
    }

    @Override
    public void redirectToOriginalUrl(
            String shortCode,
            HttpServletResponse response)
            throws IOException {

        String originalUrl = getOriginalUrl(shortCode);

        response.sendRedirect(originalUrl);
    }
    @Override
    public byte[] generateQrCode(String shortCode) {

        logger.info("Generating QR code for short code: {}", shortCode);

        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    logger.error("QR code requested for non-existing short code: {}", shortCode);
                    return new ResourceNotFoundException("Short URL not found");
                });

        String shortUrl = "http://localhost:8080/" + url.getShortCode();

        try {

            return QrCodeGenerator.generateQRCode(shortUrl, 300, 300);

        } catch (WriterException | IOException e) {

            logger.error("Failed to generate QR code.", e);

            throw new RuntimeException("Unable to generate QR code");
        }
    }

    /**
     * Generate a unique random short code.
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