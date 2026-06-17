package com.smartbus.booking.repository;

import com.smartbus.booking.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"user", "booking", "repliedBy"})
    List<Review> findByCompanyName(String companyName);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.companyName = :companyName")
    Double getAverageRatingByCompany(String companyName);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.companyName = :companyName")
    Long getReviewCountByCompany(String companyName);

    boolean existsByBookingId(Long bookingId);
    
    java.util.Optional<Review> findByBookingId(Long bookingId);
    
    List<Review> findByBookingIdIn(List<Long> bookingIds);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"user", "booking", "booking.trip", "repliedBy"})
    @Query("SELECT r FROM Review r")
    List<Review> findAllWithDetails();
}
