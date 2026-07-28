package com.smartbus.booking.repository;

import com.smartbus.booking.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findBySlug(String slug);
    List<Article> findByIsActiveTrueOrderByCreatedAtDesc();
}
