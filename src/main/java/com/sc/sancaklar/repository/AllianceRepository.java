package com.sc.sancaklar.repository;

import com.sc.sancaklar.model.entity.AllianceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AllianceRepository extends JpaRepository<AllianceEntity, Long> {
    Optional<AllianceEntity> findByTag(String tag); // Klan etiketine göre arama
    List<AllianceEntity> findAllByOrderByRankAsc(); // Sıralama tablosu
}
