package com.hemanth.distributedurlshortener.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Future;
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

    @Future(message = "Expiration date must be in the future")
    private LocalDateTime expiresAt;
}