package com.hemanth.distributedurlshortener.service.impl;

import com.google.zxing.WriterException;
import com.hemanth.distributedurlshortener.dto.request.CreateShortUrlRequest;
import com.hemanth.distributedurlshortener.dto.response.ShortUrlResponse;
import com.hemanth.distributedurlshortener.dto.response.UrlAnalyticsResponse;
import com.hemanth.distributedurlshortener.entity.Url;
import com.hemanth.distributedurlshortener.entity.User;
import com.hemanth.distributedurlshortener.exception.ResourceNotFoundException;
import com.hemanth.distributedurlshortener.exception.ShortCodeAlreadyExistsException;
import com.hemanth.distributedurlshortener.exception.UrlExpiredException;
import com.hemanth.distributedurlshortener.repository.UrlRepository;
import com.hemanth.distributedurlshortener.repository.UserRepository;
import com.hemanth.distributedurlshortener.service.UrlService;
import com.hemanth.distributedurlshortener.util.QrCodeGenerator;
import com.hemanth.distributedurlshortener.config.RedisProperties;
import com.hemanth.distributedurlshortener.kafka.UrlClickProducer;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

   @Value("${app.base-url}")
   private String baseUrl;

    private static final Logger logger =
            LoggerFactory.getLogger(UrlServiceImpl.class);


    private final UserRepository userRepository;
    private final UrlRepository urlRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisProperties redisProperties;
    private static final String CACHE_PREFIX = "url:";
    private final UrlClickProducer urlClickProducer;


    @Override
    public ShortUrlResponse createShortUrl(CreateShortUrlRequest request) {

        logger.info("Creating short URL for {}", request.getOriginalUrl());


        Optional<Url> existingUrl =
                urlRepository.findByOriginalUrl(request.getOriginalUrl());


        if (existingUrl.isPresent()) {
            return buildResponse(existingUrl.get());
        }


        String shortCode;


        if (request.getCustomShortCode() != null &&
                !request.getCustomShortCode().isBlank()) {


            shortCode = request.getCustomShortCode();


            if (urlRepository.findByShortCode(shortCode).isPresent()) {
                throw new ShortCodeAlreadyExistsException(
                        "Short code already exists"
                );
            }

        } else {

            shortCode = generateUniqueShortCode();
        }


        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();


        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));


        Url url = Url.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .createdAt(LocalDateTime.now())
                .expiresAt(request.getExpiresAt())
                .clickCount(0L)
                .user(user)
                .build();


        urlRepository.save(url);


        return buildResponse(url);
    }



   @Override
public String getOriginalUrl(String shortCode) {

    long startTime = System.currentTimeMillis();

    String cacheKey = CACHE_PREFIX + shortCode;


    /*
     * Always get the URL from PostgreSQL first.
     *
     * PostgreSQL contains the expiresAt value,
     * while Redis only contains the original URL.
     */
    Url url = urlRepository.findByShortCode(shortCode)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Short URL not found"
                    ));


    /*
     * Check expiration BEFORE Redis.
     *
     * If expiresAt is null:
     *      URL never expires.
     *
     * If expiresAt is in the past:
     *      URL has expired.
     */
    if (url.getExpiresAt() != null &&
            !LocalDateTime.now().isBefore(url.getExpiresAt())) {

        // Remove expired URL from Redis if it exists
        redisTemplate.delete(cacheKey);

        logger.info(
                "Short URL {} has expired at {}",
                shortCode,
                url.getExpiresAt()
        );

        throw new UrlExpiredException(
                "Short URL has expired"
        );
    }


    /*
     * URL is NOT expired.
     * Now check Redis.
     */
    String cachedUrl =
            redisTemplate.opsForValue().get(cacheKey);


    if (cachedUrl != null) {

        logger.info(
                "Cache HIT for {}",
                shortCode
        );

        urlClickProducer.publishClick(shortCode);

        logger.info(
                "Click event published for {}",
                shortCode
        );

        long endTime =
                System.currentTimeMillis();

        logger.info(
                "Response Time: {} ms",
                endTime - startTime
        );

        return cachedUrl;
    }


    /*
     * Redis cache MISS.
     */
    logger.info(
            "Cache MISS for {}",
            shortCode
    );


    /*
     * Only cache the URL after confirming
     * that it has not expired.
     */
    redisTemplate.opsForValue().set(
            cacheKey,
            url.getOriginalUrl(),
            java.time.Duration.ofSeconds(
                    redisProperties.getTtl()
            )
    );


    logger.info(
            "Stored {} in Redis cache",
            shortCode
    );


    long endTime =
            System.currentTimeMillis();


    logger.info(
            "Response Time: {} ms",
            endTime - startTime
    );


    return url.getOriginalUrl();
}
    @Override
    public List<ShortUrlResponse> getMyUrls() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return urlRepository.findByUser(user)
                .stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }



    @Override
    public UrlAnalyticsResponse getAnalytics(String shortCode) {


        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Short URL not found"
                        ));


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


        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Short URL not found"
                        ));


       String shortUrl = baseUrl + "/" + url.getShortCode();


        try {

            return QrCodeGenerator.generateQRCode(
                    shortUrl,
                    300,
                    300
            );

        } catch (WriterException | IOException e) {

            throw new RuntimeException(
                    "Unable to generate QR code",
                    e
            );
        }
    }




    private String generateUniqueShortCode() {


        String shortCode;


        do {

            shortCode =
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 8);


        } while (
                urlRepository.findByShortCode(shortCode)
                        .isPresent()
        );


        return shortCode;
    }

    private void evictCache(String shortCode) {

        String cacheKey =  CACHE_PREFIX + shortCode;

        redisTemplate.delete(cacheKey);

        logger.info("Removed {} from Redis cache", shortCode);
    }


    private ShortUrlResponse buildResponse(Url url) {


        return ShortUrlResponse.builder()
                .originalUrl(url.getOriginalUrl())
                .shortCode(url.getShortCode())
                .shortUrl(
                       baseUrl + "/"
                                + url.getShortCode()
                )
                .build();
    }
}
