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

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private static final Logger logger =
            LoggerFactory.getLogger(UrlServiceImpl.class);


    private final UserRepository userRepository;
    private final UrlRepository urlRepository;


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


        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Short URL not found"
                        ));


        if (url.getExpiresAt() != null &&
                LocalDateTime.now().isAfter(url.getExpiresAt())) {


            throw new UrlExpiredException(
                    "Short URL has expired"
            );
        }


        url.setClickCount(url.getClickCount() + 1);

        urlRepository.save(url);


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


        String shortUrl =
                "http://localhost:8080/" + url.getShortCode();


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




    private ShortUrlResponse buildResponse(Url url) {


        return ShortUrlResponse.builder()
                .originalUrl(url.getOriginalUrl())
                .shortCode(url.getShortCode())
                .shortUrl(
                        "http://localhost:8080/"
                                + url.getShortCode()
                )
                .build();
    }
}