package com.sc.sancaklar.repository;

import com.sc.sancaklar.model.entity.ArmyMovementEntity;
import com.sc.sancaklar.model.entity.VillageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ArmyMovementRepository extends JpaRepository<ArmyMovementEntity, Long> {
    /**
     * Varış zamanı gelmiş ve henüz Savaş/Destek işlemi görmemiş tüm hareketleri getirir.
     * Bu metot, GameLoopService tarafından çağrılır.
     * * @param now Şu anki zaman.
     * @return İşlenmesi gereken hareket listesi.
     */
    List<ArmyMovementEntity> findByArrivalTimeBeforeAndIsProcessedFalse(LocalDateTime now);

    /**
     * Belirtilen KÖYDEN gönderilmiş, hala yolda olan (isProcessed=false) hareketleri bulur.
     */
    List<ArmyMovementEntity> findBySourceVillage(VillageEntity sourceVillage);

    /**
     * Belirtilen OYUNCUYA ait köylerden gönderilmiş, hala yolda olan hareketleri bulur.
     * Bu, oyuncunun tüm köylerindeki tüm giden orduları gösterir.
     */
    List<ArmyMovementEntity> findBySourceVillage_Player_IdAndIsProcessedFalse(Long playerId);
}
