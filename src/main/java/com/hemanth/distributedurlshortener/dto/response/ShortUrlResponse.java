package com.hemanth.distributedurlshortener.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShortUrlResponse {

    private String originalUrl;

    private String shortCode;

    private String shortUrl;
}