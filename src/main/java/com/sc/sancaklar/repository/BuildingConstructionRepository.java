package com.sc.sancaklar.repository;

import com.sc.sancaklar.model.entity.BuildingConstructionEntity;
import com.sc.sancaklar.model.entity.VillageEntity;
import com.sc.sancaklar.model.enums.BuildingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BuildingConstructionRepository extends JpaRepository<BuildingConstructionEntity, Long> {
    // Köydeki tüm inşaatları bitiş süresine göre tersten getir (En son bitecek olan en üstte)
    List<BuildingConstructionEntity> findByVillageOrderByCompletionTimeDesc(VillageEntity village);

    // Bu binadan kuyrukta kaç tane var? (Target Level hesabı için)
    long countByVillageAndBuildingType(VillageEntity village, BuildingType buildingType);

    // Frontend'e listelemek için (Normal sıralama)
    List<BuildingConstructionEntity> findByVillageIdOrderByCompletionTimeAsc(Long villageId);

    // Verilen zamandan (muhtemelen ŞİMDİ) önce bitmesi gereken tüm inşaatları getir.
    List<BuildingConstructionEntity> findByCompletionTimeBefore(LocalDateTime dateTime);
}
