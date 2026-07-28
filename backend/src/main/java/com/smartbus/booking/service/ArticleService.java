package com.smartbus.booking.service;

import com.smartbus.booking.entity.Article;
import com.smartbus.booking.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    public List<Article> getAllArticlesAdmin() {
        return articleRepository.findAll();
    }

    public List<Article> getActiveArticles() {
        return articleRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    public Article getArticleById(Long id) {
        return articleRepository.findById(id).orElse(null);
    }

    public Article getArticleBySlug(String slug) {
        return articleRepository.findBySlug(slug).orElse(null);
    }

    public Article createArticle(Article article) {
        article.setSlug(generateSlug(article.getTitle()));
        article.setCreatedAt(LocalDateTime.now());
        return articleRepository.save(article);
    }

    public Article updateArticle(Long id, Article req) {
        Article existing = getArticleById(id);
        if (existing == null) return null;
        
        existing.setTitle(req.getTitle());
        // Không cập nhật slug khi edit để bảo vệ SEO và link cũ (người dùng F5 không bị lỗi 404)
        existing.setSummary(req.getSummary());
        existing.setContent(req.getContent());
        existing.setImageUrl(req.getImageUrl());
        existing.setIsActive(req.getIsActive());
        
        return articleRepository.save(existing);
    }

    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }

    private String generateSlug(String title) {
        if (title == null) return "";
        try {
            // Loại bỏ dấu tiếng Việt
            String temp = Normalizer.normalize(title, Normalizer.Form.NFD);
            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            String slug = pattern.matcher(temp).replaceAll("").toLowerCase().replace("đ", "d");
            
            // Thay thế các khoảng trắng và ký tự đặc biệt bằng dấu gạch ngang
            slug = slug.replaceAll("[^a-z0-9]+", "-");
            slug = slug.replaceAll("^-+|-+$", ""); // Xóa gạch ngang ở đầu và cuối
            
            // Thêm 4 số ngẫu nhiên từ timestamp để đảm bảo slug luôn unique
            return slug + "-" + (System.currentTimeMillis() % 10000);
        } catch (Exception e) {
            return "article-" + System.currentTimeMillis();
        }
    }
}
