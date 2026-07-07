package com.hemanth.distributedurlshortener.service.impl;

import com.hemanth.distributedurlshortener.dto.request.CreateShortUrlRequest;
import com.hemanth.distributedurlshortener.dto.response.ShortUrlResponse;
import com.hemanth.distributedurlshortener.entity.Url;
import com.hemanth.distributedurlshortener.repository.UrlRepository;
import com.hemanth.distributedurlshortener.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;

    @Override
    public ShortUrlResponse createShortUrl(CreateShortUrlRequest request) {

        String shortCode = UUID.randomUUID()
                .toString()
                .substring(0,8);

        Url url = Url.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .createdAt(LocalDateTime.now())
                .clickCount(0L)
                .build();

        urlRepository.save(url);

        return ShortUrlResponse.builder()
                .originalUrl(url.getOriginalUrl())
                .shortCode(shortCode)
                .shortUrl("http://localhost:8080/" + shortCode)
                .build();
    }
}