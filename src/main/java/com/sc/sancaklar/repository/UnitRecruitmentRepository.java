package com.sc.sancaklar.repository;

import com.sc.sancaklar.model.entity.UnitRecruitmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UnitRecruitmentRepository extends JpaRepository<UnitRecruitmentEntity, Long> {
    List<UnitRecruitmentEntity> findByVillageIdOrderByCompletionTimeAsc(Long villageId);

    // Bitmiş asker üretimlerini getir
    List<UnitRecruitmentEntity> findByCompletionTimeBefore(LocalDateTime now);
}
