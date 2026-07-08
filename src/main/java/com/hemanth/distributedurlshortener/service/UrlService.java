package com.hemanth.distributedurlshortener.service;

import com.hemanth.distributedurlshortener.dto.request.CreateShortUrlRequest;
import com.hemanth.distributedurlshortener.dto.response.ShortUrlResponse;

public interface UrlService {


    ShortUrlResponse createShortUrl(CreateShortUrlRequest request);

    String getOriginalUrl(String shortCode);
}