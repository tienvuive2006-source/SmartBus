package com.smartbus.booking.controller;

import com.smartbus.booking.entity.Article;
import com.smartbus.booking.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ArticleController {

    private final ArticleService articleService;

    // --- API DÀNH CHO KHÁCH HÀNG (PUBLIC) ---
    @GetMapping
    public ResponseEntity<List<Article>> getActiveArticles() {
        return ResponseEntity.ok(articleService.getActiveArticles());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Article> getArticleBySlug(@PathVariable("slug") String slug) {
        Article article = articleService.getArticleBySlug(slug);
        if (article == null || !article.getIsActive()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(article);
    }

    // --- API DÀNH CHO QUẢN TRỊ VIÊN (ADMIN) ---
    @GetMapping("/all")
    public ResponseEntity<List<Article>> getAllArticlesAdmin() {
        return ResponseEntity.ok(articleService.getAllArticlesAdmin());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Article> getArticleById(@PathVariable("id") Long id) {
        Article article = articleService.getArticleById(id);
        return article != null ? ResponseEntity.ok(article) : ResponseEntity.notFound().build();
    }

    @com.smartbus.booking.annotation.AuditAction(action = "CREATE_ARTICLE", entityName = "Article")
    @PostMapping
    public ResponseEntity<Article> createArticle(@RequestBody Article article) {
        return ResponseEntity.ok(articleService.createArticle(article));
    }

    @com.smartbus.booking.annotation.AuditAction(action = "UPDATE_ARTICLE", entityName = "Article")
    @PutMapping("/{id}")
    public ResponseEntity<Article> updateArticle(@PathVariable("id") Long id, @RequestBody Article article) {
        Article updated = articleService.updateArticle(id, article);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @com.smartbus.booking.annotation.AuditAction(action = "DELETE_ARTICLE", entityName = "Article")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable("id") Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }
}
