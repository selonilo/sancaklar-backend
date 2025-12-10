package com.sc.sancaklar.repository;

import com.sc.sancaklar.model.data.GameCalculator;
import com.sc.sancaklar.model.entity.UnitRecruitmentEntity;
import com.sc.sancaklar.model.entity.VillageEntity;
import com.sc.sancaklar.model.enums.BuildingType;
import com.sc.sancaklar.model.enums.UnitType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UnitRecruitmentRepository extends JpaRepository<UnitRecruitmentEntity, Long> {
    // Bir köydeki belirli bir askerin kuyruğunu getir (Süre hesabına katmak için)
    // Ancak daha doğrusu: O BİNADAKİ kuyruğu getirmektir.
    // Kışla kuyruğu ayrı, Ahır kuyruğu ayrı çalışır.

    // Bizim UnitRecruitmentEntity'de 'unitType' var ama 'buildingType' yok.
    // Sorun değil, unitType'a bakıp hangi bina olduğunu anlayabiliriz.
    // Basitlik adına köydeki tüm üretimleri çekip Java'da filtreleyebiliriz.
    List<UnitRecruitmentEntity> findByVillageIdOrderByCompletionTimeDesc(Long villageId);

    // Game Loop için
    List<UnitRecruitmentEntity> findByCompletionTimeBefore(LocalDateTime now);

    // Görüntüleme için (Bitiş zamanı artan sıralı)
    List<UnitRecruitmentEntity> findByVillageIdOrderByCompletionTimeAsc(Long villageId);

    Optional<UnitRecruitmentEntity> findFirstByVillageAndBuildingTypeOrderByCompletionTimeDesc(
            VillageEntity village,
            BuildingType buildingType
    );

    // Eğer entity'e buildingType eklemezsen JPQL ile join atman veya UnitType listesi vermen gerekir:
    @Query("SELECT r FROM UnitRecruitmentEntity r WHERE r.village = :village AND r.unitType IN :unitTypes ORDER BY r.completionTime DESC")
    List<UnitRecruitmentEntity> findLatestQueueItem(@Param("village") VillageEntity village, @Param("unitTypes") List<UnitType> unitTypes, Pageable pageable);

    default Optional<UnitRecruitmentEntity> findLastByVillageAndBuildingType(VillageEntity village, BuildingType building) {
        // BuildingType'a göre UnitType listesini belirle
        List<UnitType> types = GameCalculator.getUnitsForBuilding(building);
        List<UnitRecruitmentEntity> results = findLatestQueueItem(village, types, PageRequest.of(0, 1));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
