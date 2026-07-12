package com.hemanth.distributedurlshortener.controller;

import com.hemanth.distributedurlshortener.dto.request.CreateShortUrlRequest;
import com.hemanth.distributedurlshortener.dto.response.ShortUrlResponse;
import com.hemanth.distributedurlshortener.response.ApiResponse;
import com.hemanth.distributedurlshortener.service.UrlService;
import com.hemanth.distributedurlshortener.dto.response.UrlAnalyticsResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/urls")
public class UrlController {

    private final UrlService urlService;

    // Constructor Injection
    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    // Create Short URL
    @PostMapping
    public ResponseEntity<ApiResponse<ShortUrlResponse>> createShortUrl(
            @Valid @RequestBody CreateShortUrlRequest request) {

        // Call service layer
        ShortUrlResponse response = urlService.createShortUrl(request);

        // Build standard API response
        ApiResponse<ShortUrlResponse> apiResponse =
                ApiResponse.<ShortUrlResponse>builder()
                        .success(true)
                        .message("Short URL created successfully")
                        .data(response)
                        .timestamp(LocalDateTime.now())
                        .build();

        // Return HTTP 201 Created
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponse);
    }
    @GetMapping("/{shortCode}/analytics")
    public ResponseEntity<ApiResponse<UrlAnalyticsResponse>> getAnalytics(
            @PathVariable String shortCode) {

        UrlAnalyticsResponse response = urlService.getAnalytics(shortCode);

        ApiResponse<UrlAnalyticsResponse> apiResponse = ApiResponse.<UrlAnalyticsResponse>builder()
                .success(true)
                .message("Analytics fetched successfully")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}