package com.hemanth.distributedurlshortener.repository;

import com.hemanth.distributedurlshortener.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hemanth.distributedurlshortener.entity.User;
import java.util.Optional;
import java.util.List;

public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(String shortCode);
    Optional<Url> findByOriginalUrl(String originalUrl);
    boolean existsByShortCode(String shortCode);
    List<Url> findByUser(User user);
}