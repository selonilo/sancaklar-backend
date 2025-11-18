package com.sc.sancaklar.repository;

import com.sc.sancaklar.model.entity.UnitRecruitmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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
}
