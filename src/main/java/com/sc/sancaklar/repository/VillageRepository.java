package com.sc.sancaklar.repository;

import com.sc.sancaklar.model.entity.PlayerEntity;
import com.sc.sancaklar.model.entity.VillageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VillageRepository extends JpaRepository<VillageEntity, Long> {
    // Oyuncunun tüm köyleri
    List<VillageEntity> findByPlayer(PlayerEntity player);

    // Haritada belirli bir koordinattaki köyü bul (Fetih ve Saldırı için kritik)
    Optional<VillageEntity> findByXcoordAndYcoord(int x, int y);

    // Haritada belirli bir bölgedeki köyleri çekmek için (Haritayı kaydırırken)
    // Örn: X: 500-520, Y: 400-420 arası
    List<VillageEntity> findByXcoordBetweenAndYcoordBetween(int xMin, int xMax, int yMin, int yMax);
}
