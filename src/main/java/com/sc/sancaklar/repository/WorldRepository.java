package com.sc.sancaklar.repository;

import com.sc.sancaklar.model.entity.WorldEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorldRepository extends JpaRepository<WorldEntity, Long> {
    List<WorldEntity> findByIsActiveTrue(); // Sadece aktif dünyaları listele
}
