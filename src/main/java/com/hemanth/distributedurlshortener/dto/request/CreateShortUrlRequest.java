package com.hemanth.distributedurlshortener.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShortUrlRequest {

    @NotBlank(message = "Original URL cannot be empty")
    @Pattern(
            regexp = "^(https?://).+",
            message = "URL must start with http:// or https://"
    )
    private String originalUrl;
    @Size(
            min = 3,
            max = 20,
            message = "Custom short code must be between 3 and 20 characters"
    )
    @Pattern(
            regexp = "^[a-zA-Z0-9_-]*$",
            message = "Custom short code can contain only letters, numbers, hyphens and underscores"
    )
    private String customShortCode;


    @Future(message = "Expiration date must be in the future")
    private LocalDateTime expiresAt;
}