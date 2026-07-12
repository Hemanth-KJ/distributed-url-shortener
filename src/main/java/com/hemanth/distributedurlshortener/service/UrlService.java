package com.hemanth.distributedurlshortener.service;

import com.hemanth.distributedurlshortener.dto.request.CreateShortUrlRequest;
import com.hemanth.distributedurlshortener.dto.response.ShortUrlResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface UrlService {

    ShortUrlResponse createShortUrl(CreateShortUrlRequest request);

    String getOriginalUrl(String shortCode);

    void redirectToOriginalUrl(String shortCode,
                               HttpServletResponse response)
            throws IOException;
}