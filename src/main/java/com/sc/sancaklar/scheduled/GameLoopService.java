package com.sc.sancaklar.scheduled;

import com.sc.sancaklar.model.data.GameCalculator;
import com.sc.sancaklar.model.entity.*;
import com.sc.sancaklar.model.enums.MovementType;
import com.sc.sancaklar.repository.*;
import com.sc.sancaklar.service.BattleService;
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
    private final ResearchQueueRepository researchQueueRepository;
    private final UnitRecruitmentRepository unitRecruitmentRepository;
    private final VillageTroopsRepository villageTroopsRepository;
    private final ArmyMovementRepository movementRepository;
    private final BattleService battleService;
    private final StationedTroopsRepository stationedTroopsRepository;

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

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void checkCompletedResearches() {
        LocalDateTime now = LocalDateTime.now();
        List<ResearchQueueEntity> completed = researchQueueRepository.findByCompletionTimeBefore(now);

        for (ResearchQueueEntity task : completed) {
            completeResearch(task);
        }

        if (!completed.isEmpty()) {
            researchQueueRepository.deleteAll(completed);
        }
    }

    private void completeResearch(ResearchQueueEntity task) {
        VillageEntity village = task.getVillage();
        VillageResearchesEntity researches = village.getResearches();

        // İlgili birimi '1' (Açık) yap
        switch (task.getUnitType()) {
            // --- PİYADELER ---
            case SPEARMAN:
                researches.setSpearmen(1);
                break;
            case SWORDSMAN:
                researches.setSwordsmen(1);
                break;
            case AXEMAN:
                researches.setAxemen(1);
                break;
            case ARCHER:
                researches.setArchers(1);
                break;

            // --- ATLILAR VE CASUS ---
            case SCOUT:
                researches.setScouts(1);
                break;
            case LIGHT_CAVALRY:
                researches.setLightCavalry(1);
                break;
            case HEAVY_CAVALRY:
                researches.setHeavyCavalry(1);
                break;

            // --- KUŞATMA SİLAHLARI ---
            case RAM:
                researches.setRams(1);
                break;
            case CATAPULT:
                researches.setCatapults(1);
                break;

            // --- ÖZEL BİRİM ---
            case CONQUEROR:
                researches.setConquerors(1);
                break;
        }
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void checkCompletedRecruitments() {
        LocalDateTime now = LocalDateTime.now();
        List<UnitRecruitmentEntity> completed = unitRecruitmentRepository.findByCompletionTimeBefore(now);

        for (UnitRecruitmentEntity task : completed) {
            completeRecruitment(task);
        }

        if (!completed.isEmpty()) {
            unitRecruitmentRepository.deleteAll(completed);
        }
    }

    private void completeRecruitment(UnitRecruitmentEntity task) {
        VillageEntity village = task.getVillage();
        VillageTroopsEntity troops = village.getTroops();
        int amount = task.getQuantity();

        switch (task.getUnitType()) {
            case SPEARMAN: troops.setSpearmen(troops.getSpearmen() + amount); break;
            case SWORDSMAN: troops.setSwordsmen(troops.getSwordsmen() + amount); break;
            case AXEMAN: troops.setAxemen(troops.getAxemen() + amount); break;
            case ARCHER: troops.setArchers(troops.getArchers() + amount); break;

            case SCOUT: troops.setScouts(troops.getScouts() + amount); break;
            case LIGHT_CAVALRY: troops.setLightCavalry(troops.getLightCavalry() + amount); break;
            case HEAVY_CAVALRY: troops.setHeavyCavalry(troops.getHeavyCavalry() + amount); break;

            case RAM: troops.setRams(troops.getRams() + amount); break;
            case CATAPULT: troops.setCatapults(troops.getCatapults() + amount); break;

            case CONQUEROR: troops.setConquerors(troops.getConquerors() + amount); break;
        }

        villageTroopsRepository.save(troops);
    }

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void checkMovements() {
        LocalDateTime now = LocalDateTime.now();
        // Varış zamanı gelmiş hareketler
        List<ArmyMovementEntity> movements = movementRepository.findByArrivalTimeBeforeAndIsProcessedFalse(now);

        for (ArmyMovementEntity move : movements) {
            processMovement(move);
        }
    }

    private void processMovement(ArmyMovementEntity move) {

        // --- 1. SAVAŞ/DESTEK/DÖNÜŞ MANTIĞI ---
        if (move.getType() == MovementType.ATTACK) {
            // Savaş motoru çalışır, sonuçları veritabanına uygular
            battleService.resolveBattle(move);

        } else if (move.getType() == MovementType.SUPPORT) {
            // Destek askerleri hedef köye yerleşir
            placeSupportTroops(move);

        } else if (move.getType() == MovementType.RETURN) {
            // Askerler ve ganimet köye eklenir
            returnTroopsHome(move);
        }

        // --- 2. İŞLEMİN KAYIT ALTINA ALINMASI (BAYRAĞI ÇEKME) ---

        // 1. İşlem tamamlandığı için bayrağı TRUE'ya çekiyoruz.
        move.setProcessed(true);

        // 2. Hareket kaydını veritabanında GÜNCELLE (Silme yerine, kayıt için tutuyoruz)
        movementRepository.save(move);
    }

    // Destek askerlerini köye yerleştirme
    private void placeSupportTroops(ArmyMovementEntity move) {
        StationedTroopsEntity station = new StationedTroopsEntity();
        station.setLocationVillage(move.getTargetVillage());
        station.setOwnerVillage(move.getSourceVillage());

        station.setSpearmen(move.getSpearmen());
        station.setSwordsmen(move.getSwordsmen());
        // ... diğerlerini set et

        stationedTroopsRepository.save(station);
    }

    // Askerler eve döndü, ana stoğa ekle
    private void returnTroopsHome(ArmyMovementEntity move) {
        VillageEntity home = move.getTargetVillage(); // Dönüşte target = evdir
        VillageTroopsEntity troops = home.getTroops();
        VillageResourcesEntity res = home.getResources();

        // Askerleri ekle
        troops.setSpearmen(troops.getSpearmen() + move.getSpearmen());
        // ...

        // Ganimeti ekle
        res.setWoodAmount(res.getWoodAmount() + move.getWoodCarried());
        res.setMeatAmount(res.getMeatAmount() + move.getMeatCarried());
        res.setIronAmount(res.getIronAmount() + move.getIronCarried());

        // Kaydet
    }
}