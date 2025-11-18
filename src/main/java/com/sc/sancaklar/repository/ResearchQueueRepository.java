package com.sc.sancaklar.repository;

import com.sc.sancaklar.model.entity.ResearchQueueEntity;
import com.sc.sancaklar.model.entity.VillageEntity;
import com.sc.sancaklar.model.enums.UnitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ResearchQueueRepository extends JpaRepository<ResearchQueueEntity, Long> {
    // 1. Kontrol: Bu köyde, bu birim için şu an kuyrukta bekleyen bir işlem var mı?
    // Bunu "Aynı anda iki kere Kılıçlı araştırmasın" diye kullanıyoruz.
    boolean existsByVillageAndUnitType(VillageEntity village, UnitType unitType);

    // 2. Game Loop: Süresi dolan (bitmiş) araştırmaları bulmak için.
    List<ResearchQueueEntity> findByCompletionTimeBefore(LocalDateTime now);

    List<ResearchQueueEntity> findByVillageIdOrderByCompletionTimeAsc(Long villageId);
}
