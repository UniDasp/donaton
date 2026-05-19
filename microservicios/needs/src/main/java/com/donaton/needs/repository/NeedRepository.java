package com.donaton.needs.repository;

import com.donaton.needs.model.NeedEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NeedRepository extends JpaRepository<NeedEntity, String> {
    List<NeedEntity> findByStatus(String status);
    List<NeedEntity> findByCategoryAndStatus(String category, String status);
    List<NeedEntity> findByCategory(String category);
}
