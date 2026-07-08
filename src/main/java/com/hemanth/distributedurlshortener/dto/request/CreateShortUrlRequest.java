package com.hemanth.distributedurlshortener.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class CreateShortUrlRequest {

    @NotBlank(message = "Original URL cannot be empty")
    @URL(message = "Please enter a valid URL")
    private String originalUrl;
}