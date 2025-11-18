package com.sc.sancaklar.service;

import com.sc.sancaklar.model.data.GameCalculator;
import com.sc.sancaklar.model.entity.BuildingConstructionEntity;
import com.sc.sancaklar.model.entity.PlayerEntity;
import com.sc.sancaklar.model.entity.VillageBuildingsEntity;
import com.sc.sancaklar.model.entity.VillageEntity;
import com.sc.sancaklar.repository.BuildingConstructionRepository;
import com.sc.sancaklar.repository.PlayerRepository;
import com.sc.sancaklar.repository.VillageBuildingsRepository;
import com.sc.sancaklar.repository.VillageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameLoopService {

    private final BuildingConstructionRepository constructionRepository;
    private final VillageRepository villageRepository;
    private final PlayerRepository playerRepository;
    private final VillageBuildingsRepository buildingsRepository;

    /**
     * Her 1000 milisaniyede (1 saniye) bir çalışır.
     * "fixedRate" önceki görevin başlamasından itibaren süreyi sayar.
     */
    @Scheduled(fixedRate = 1000)
    @Transactional // Bir hata olursa o saniyelik işlem geri alınsın
    public void checkCompletedConstructions() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Süresi dolmuş inşaatları getir
        List<BuildingConstructionEntity> completedTasks = constructionRepository.findByCompletionTimeBefore(now);

        if (completedTasks.isEmpty()) return; // İş yoksa yorma

        // 2. Her bir görevi işle
        for (BuildingConstructionEntity task : completedTasks) {
            completeConstruction(task);
        }

        // 3. İşlenen görevleri kuyruktan sil (Toplu silme performansı artırır)
        constructionRepository.deleteAll(completedTasks);

        // Log basabilirsin: System.out.println(completedTasks.size() + " bina tamamlandı.");
    }

    private void completeConstruction(BuildingConstructionEntity task) {
        VillageEntity village = task.getVillage();
        VillageBuildingsEntity buildings = village.getBuildings();
        PlayerEntity player = village.getPlayer();

        // A. Bina Seviyesini Güncelle
        updateBuildingLevel(buildings, task);
        buildingsRepository.save(buildings);

        // B. Puan Hesapla ve Ekle
        // (Basit mantık: Seviye başına gelen puanı ekliyoruz)
        int pointsToAdd = GameCalculator.calculateBuildingPoints(task.getBuildingType(), task.getTargetLevel());

        // Köy puanı artır
        village.setPoints(village.getPoints() + pointsToAdd);
        villageRepository.save(village);

        // Oyuncu puanı artır
        if (player != null) {
            player.setPoints(player.getPoints() + pointsToAdd);
            playerRepository.save(player);
        }
    }

    private void updateBuildingLevel(VillageBuildingsEntity buildings, BuildingConstructionEntity task) {
        int level = task.getTargetLevel();

        switch (task.getBuildingType()) {
            case HEADQUARTERS: buildings.setHeadquarters(level); break;
            case BARRACKS:     buildings.setBarracks(level); break;
            case STABLE:       buildings.setStable(level); break;
            case WORKSHOP:     buildings.setWorkshop(level); break;
            case ACADEMY:      buildings.setAcademy(level); break;
            case SMITHY:       buildings.setSmithy(level); break;
            case MARKET:       buildings.setMarket(level); break;
            case TIMBER_CAMP:  buildings.setTimberCamp(level); break;
            case MEAT_PLANT:   buildings.setMeatProduction(level); break;
            case IRON_MINE:    buildings.setIronMine(level); break;
            case FARM:         buildings.setFarm(level); break;
            case WAREHOUSE:    buildings.setWarehouse(level); break;
            case WALL:         buildings.setWall(level); break;
        }
    }
}