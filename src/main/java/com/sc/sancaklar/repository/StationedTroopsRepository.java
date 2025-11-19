package com.sc.sancaklar.repository;

import com.sc.sancaklar.model.entity.StationedTroopsEntity;
import com.sc.sancaklar.model.entity.VillageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StationedTroopsRepository extends JpaRepository<StationedTroopsEntity, Long> {
    /**
     * Savunma gücünü hesaplamak için belirli bir köye (locationVillage) gönderilmiş
     * tüm destek birliklerini getirir.
     * @param locationVillage Savunulan köyün Entity'si.
     * @return Bu köyde konuşlanmış ordu listesi.
     */
    List<StationedTroopsEntity> findByLocationVillage(VillageEntity locationVillage);
}
