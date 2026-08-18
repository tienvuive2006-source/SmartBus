package com.smartbus.booking.repository;

import com.smartbus.booking.entity.SavedLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedLocationRepository extends JpaRepository<SavedLocation, Long> {
    @Query("SELECT location FROM SavedLocation location ORDER BY location.id ASC")
    List<SavedLocation> findAllByOrderByNameAsc();
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
