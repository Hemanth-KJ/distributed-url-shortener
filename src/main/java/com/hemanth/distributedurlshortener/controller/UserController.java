package com.hemanth.distributedurlshortener.controller;

import com.hemanth.distributedurlshortener.dto.response.ShortUrlResponse;
import com.hemanth.distributedurlshortener.response.ApiResponse;
import com.hemanth.distributedurlshortener.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UrlService urlService;

    @GetMapping("/me/urls")
    public ResponseEntity<ApiResponse<List<ShortUrlResponse>>> getMyUrls() {

        List<ShortUrlResponse> urls = urlService.getMyUrls();

        ApiResponse<List<ShortUrlResponse>> response =
                ApiResponse.<List<ShortUrlResponse>>builder()
                        .success(true)
                        .message("User URLs fetched successfully")
                        .data(urls)
                        .build();

        return ResponseEntity.ok(response);
    }
}