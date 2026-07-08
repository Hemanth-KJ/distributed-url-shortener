package com.hemanth.distributedurlshortener.controller;

import com.hemanth.distributedurlshortener.service.UrlService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class RedirectController {

    private final UrlService urlService;

    @GetMapping("/{shortCode}")
    public void redirectToOriginalUrl(
            @PathVariable String shortCode,
            HttpServletResponse response) throws IOException {

        String originalUrl = urlService.getOriginalUrl(shortCode);

        response.sendRedirect(originalUrl);
    }
}