package com.hemanth.distributedurlshortener.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hemanth.distributedurlshortener.config.RateLimitConfig;
import com.hemanth.distributedurlshortener.response.ErrorResponse;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(RateLimitFilter.class);

    private final RateLimitConfig rateLimitConfig;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Apply rate limiting only to POST /api/v1/urls
        if (!request.getRequestURI().equals("/api/v1/urls")
                || !"POST".equalsIgnoreCase(request.getMethod())) {

            filterChain.doFilter(request, response);
            return;
        }

        String ipAddress = request.getRemoteAddr();

        Bucket bucket = rateLimitConfig.resolveBucket(ipAddress);
        logger.info("Available tokens before consume: {}", bucket.getAvailableTokens());
        if (bucket.tryConsume(1)) {

            logger.info(
                    "Request allowed for IP: {} | Remaining tokens: {}",
                    ipAddress,
                    bucket.getAvailableTokens()
            );

            filterChain.doFilter(request, response);

        } else {

            logger.warn("Rate limit exceeded for IP: {}", ipAddress);

            ErrorResponse errorResponse = ErrorResponse.builder()
                    .success(false)
                    .message("Rate limit exceeded. Please try again later.")
                    .errors(null)
                    .timestamp(LocalDateTime.now())
                    .build();

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");

            objectMapper.writeValue(response.getWriter(), errorResponse);
        }
    }
}