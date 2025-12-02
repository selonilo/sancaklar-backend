package com.sc.sancaklar.service;

import com.sc.sancaklar.model.data.GameCalculator;
import com.sc.sancaklar.model.dto.ConstructionModel;
import com.sc.sancaklar.model.dto.PlayerModel;
import com.sc.sancaklar.model.dto.UpgradeBuildingRequest;
import com.sc.sancaklar.model.dto.village.VillageBuildingsModel;
import com.sc.sancaklar.model.dto.village.VillageModel;
import com.sc.sancaklar.model.entity.*;
import com.sc.sancaklar.model.enums.BuildingType;
import com.sc.sancaklar.model.enums.RegionDirection;
import com.sc.sancaklar.model.mapper.ConstructionConverter;
import com.sc.sancaklar.model.mapper.VillageConverter;
import com.sc.sancaklar.repository.*;
import lombok.RequiredArgsConstructor;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class VillageServiceImpl implements VillageService {

    private final VillageRepository villageRepository;
    private final VillageBuildingsRepository villageBuildingsRepository;
    private final VillageResourcesRepository villageResourcesRepository;
    private final VillageTroopsRepository villageTroopsRepository;
    private final VillageResearchesRepository villageResearchesRepository;
    private final PlayerRepository playerRepository;
    private final VillageConverter villageConverter;
    private final ResourceService resourceService;
    private final BuildingConstructionRepository constructionRepository;
    private final ConstructionConverter constructionConverter;

    // YENİ EKLENDİ: JobRunr Servisi
    private final JobScheduler jobScheduler;

    private static final int MAP_SIZE = 1000;

    @Transactional
    @Override
    public VillageModel getVillageById(Long villageId) {
        VillageEntity village = villageRepository.findById(villageId)
                .orElseThrow(() -> new RuntimeException("Köy bulunamadı! ID: " + villageId));

        // 1. Kaynakları güncelle
        resourceService.calculateAndUpdateResources(village);

        var villageModel = villageConverter.toModel(village);
        var buildingsModel = villageModel.getBuildings();

        List<BuildingConstructionEntity> allConstructions = constructionRepository.findByVillage(village);

        int hqLevel = villageModel.getBuildings().getHeadquarters();
        int worldSpeed = village.getPlayer().getWorld().getSpeed();

        for (BuildingType type : BuildingType.values()) {

            int currentLevel = getBuildingLevel(buildingsModel, type);

            long queuedCount = allConstructions.stream()
                    .filter(c -> c.getBuildingType() == type)
                    .count();

            int targetLevel = currentLevel + (int) queuedCount + 1;

            int costWood = GameCalculator.calculateBuildingCost(type, targetLevel, "WOOD");
            int costMeat = GameCalculator.calculateBuildingCost(type, targetLevel, "MEAT");
            int costIron = GameCalculator.calculateBuildingCost(type, targetLevel, "IRON");

            long durationSeconds = GameCalculator.calculateConstructionTimeInSeconds(type, targetLevel, hqLevel, worldSpeed);

            setBuildingDetails(buildingsModel, type, costWood, costMeat, costIron, durationSeconds);
        }

        return villageModel;
    }

    @Transactional
    @Override
    public VillageModel createFirstVillage(PlayerModel playerModel, RegionDirection direction) {

        PlayerEntity playerEntity = playerRepository.findById(playerModel.getId())
                .orElseThrow(() -> new RuntimeException("Oyuncu bulunamadı!"));

        int[] coords = findCoordinatesByRegion(direction);

        VillageEntity village = new VillageEntity();
        village.setName(playerEntity.getUser().getUsername() + " Köyü");
        village.setPlayer(playerEntity);
        village.setXcoord(coords[0]);
        village.setYcoord(coords[1]);
        village.setPoints(26);
        village.setLoyalty(100);

        village = villageRepository.save(village);

        createInitialBuildings(village);
        createInitialResources(village);
        createInitialTroops(village);
        createInitialResearches(village);

        return villageConverter.toModel(village);
    }

    // --- CONSTRUCTION LOGIC (GÜNCELLENDİ) ---

    @Transactional
    public void upgradeBuilding(UpgradeBuildingRequest request) {
        // 1. Köyü Bul
        VillageEntity village = villageRepository.findById(request.getVillageId())
                .orElseThrow(() -> new RuntimeException("Köy bulunamadı!"));

        // 2. Kaynakları GÜNCELLE
        resourceService.calculateAndUpdateResources(village);

        VillageResourcesEntity resources = village.getResources();
        VillageBuildingsEntity buildings = village.getBuildings();

        // 3. Binanın Mevcut Seviyesini Bul
        int currentLevel = getBuildingLevel(buildings, request.getBuildingType());

        // 3.1. Kuyruk hesaplama (Bu bina türünden kaç tane sırada var?)
        long queuedCount = constructionRepository.countByVillageAndBuildingType(village, request.getBuildingType());
        int targetLevel = currentLevel + (int) queuedCount + 1;

        // 4. Maliyet Hesapla
        int costWood = GameCalculator.calculateBuildingCost(request.getBuildingType(), targetLevel, "WOOD");
        int costMeat = GameCalculator.calculateBuildingCost(request.getBuildingType(), targetLevel, "MEAT");
        int costIron = GameCalculator.calculateBuildingCost(request.getBuildingType(), targetLevel, "IRON");

        // 5. Yeterli Kaynak Var mı?
        if (resources.getWoodAmount() < costWood ||
                resources.getMeatAmount() < costMeat ||
                resources.getIronAmount() < costIron) {
            throw new RuntimeException("Yetersiz Kaynak!");
        }

        // 6. Kaynakları Düş (Kaynak peşin alınır)
        resources.setWoodAmount(resources.getWoodAmount() - costWood);
        resources.setMeatAmount(resources.getMeatAmount() - costMeat);
        resources.setIronAmount(resources.getIronAmount() - costIron);

        // 7. İnşaat Süresini Hesapla
        int hqLevel = buildings.getHeadquarters();
        int worldSpeed = village.getPlayer().getWorld().getSpeed();
        long durationSeconds = GameCalculator.calculateConstructionTimeInSeconds(request.getBuildingType(), targetLevel, hqLevel, worldSpeed);

        // --- 8. ZAMANLAMA VE KUYRUK MANTIĞI (GÜNCELLENDİ) ---
        LocalDateTime startTime = LocalDateTime.now();

        // Veritabanından o köydeki en son bitecek inşaatı çekiyoruz (Sadece 1 kayıt)
        Optional<BuildingConstructionEntity> lastConstruction = constructionRepository
                .findFirstByVillageOrderByCompletionTimeDesc(village);

        if (lastConstruction.isPresent()) {
            LocalDateTime lastEndTime = lastConstruction.get().getCompletionTime();

            // Eğer kuyruktaki son işin bitiş zamanı şu andan ilerideyse,
            // Bizim işimiz o bittikten tam 1 saniye sonra başlasın (veya hemen peşine)
            if (lastEndTime.isAfter(startTime)) {
                startTime = lastEndTime;
            }
        }

        // Bitiş zamanı = (Hesaplanan Başlangıç) + (Süre)
        LocalDateTime completionTime = startTime.plusSeconds(durationSeconds);

        // 9. Emri DB'ye Kaydet
        BuildingConstructionEntity construction = new BuildingConstructionEntity();
        construction.setVillage(village);
        construction.setBuildingType(request.getBuildingType());
        construction.setTargetLevel(targetLevel);
        construction.setStartTime(startTime);
        construction.setCompletionTime(completionTime);

        construction = constructionRepository.save(construction);

        // 10. JOBRUNR ZAMANLAMASI
        // JobRunr'a sadece "BİTİŞ" zamanını veriyoruz.
        // Başlangıç zamanı sadece Frontend'de progress bar göstermek için veritabanında durur.
        // Backend için önemli olan "Ne zaman bitecek?" sorusudur.
        OffsetDateTime jobTime = completionTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();

        final Long constructionId = construction.getId();

        var scheduledJobId = jobScheduler.schedule(jobTime,
                () -> completeConstructionJob(constructionId)
        );

        // 11. Job ID'yi kaydet
        construction.setJobId(scheduledJobId.asUUID().toString());
        constructionRepository.save(construction);
    }

    // --- WORKER METHOD (Zamanı Gelince Burası Çalışır) ---

    @Job(name = "Building Construction Completion") // Dashboard'da görünen isim
    @Transactional
    public void completeConstructionJob(Long constructionId) {
        // 1. Görevi bul
        BuildingConstructionEntity task = constructionRepository.findById(constructionId).orElse(null);

        if (task == null) {
            // Eğer görev DB'den silinmişse (iptal edilmişse), işlem yapma.
            return;
        }

        VillageEntity village = task.getVillage();
        VillageBuildingsEntity buildings = village.getBuildings();

        // 2. Bina Seviyesini Yükselt (DB'ye yaz)
        applyBuildingUpgrade(buildings, task.getBuildingType(), task.getTargetLevel());
        villageBuildingsRepository.save(buildings);

        // 3. Puan Hesapla (Opsiyonel ama önerilir)
        // int pointsToAdd = GameCalculator.calculatePoints(task.getBuildingType(), task.getTargetLevel());
        // village.setPoints(village.getPoints() + pointsToAdd);
        // villageRepository.save(village);

        // 4. Görevi Kuyruktan Sil
        constructionRepository.delete(task);

        System.out.println("İnşaat tamamlandı: Köy " + village.getId() + " - " + task.getBuildingType());
    }

    private int getBuildingLevel(VillageBuildingsModel model, BuildingType type) {
        return switch (type) {
            case headquarters -> model.getHeadquarters();
            case barracks -> model.getBarracks();
            case stable -> model.getStable();
            case workshop -> model.getWorkshop();
            case academy -> model.getAcademy();
            case smithy -> model.getSmithy();
            case market -> model.getMarket();
            case timberCamp -> model.getTimberCamp();
            case meatPlant -> model.getMeatPlant();
            case ironMine -> model.getIronMine();
            case farm -> model.getFarm();
            case warehouse -> model.getWarehouse();
            case wall -> model.getWall();
        };
    }

    private void setBuildingDetails(VillageBuildingsModel model, BuildingType type, int wood, int meat, int iron, long duration) {
        switch (type) {
            case headquarters -> {
                model.setHeadquartersCostWood(wood);
                model.setHeadquartersCostMeat(meat);
                model.setHeadquartersCostIron(iron);
                model.setHeadquartersDuration(duration); // <-- Yeni eklendi
            }
            case barracks -> {
                model.setBarracksCostWood(wood);
                model.setBarracksCostMeat(meat);
                model.setBarracksCostIron(iron);
                model.setBarracksDuration(duration);
            }
            case stable -> {
                model.setStableCostWood(wood);
                model.setStableCostMeat(meat);
                model.setStableCostIron(iron);
                model.setStableDuration(duration);
            }
            case workshop -> {
                model.setWorkshopCostWood(wood);
                model.setWorkshopCostMeat(meat);
                model.setWorkshopCostIron(iron);
                model.setWorkshopDuration(duration);
            }
            case academy -> {
                model.setAcademyCostWood(wood);
                model.setAcademyCostMeat(meat);
                model.setAcademyCostIron(iron);
                model.setAcademyDuration(duration);
            }
            case smithy -> {
                model.setSmithyCostWood(wood);
                model.setSmithyCostMeat(meat);
                model.setSmithyCostIron(iron);
                model.setSmithyDuration(duration);
            }
            case market -> {
                model.setMarketCostWood(wood);
                model.setMarketCostMeat(meat);
                model.setMarketCostIron(iron);
                model.setMarketDuration(duration);
            }
            case timberCamp -> {
                model.setTimberCampCostWood(wood);
                model.setTimberCampCostMeat(meat);
                model.setTimberCampCostIron(iron);
                model.setTimberCampDuration(duration);
            }
            case meatPlant -> {
                model.setMeatPlantCostWood(wood);
                model.setMeatPlantCostMeat(meat);
                model.setMeatPlantCostIron(iron);
                model.setMeatPlantDuration(duration);
            }
            case ironMine -> {
                model.setIronMineCostWood(wood);
                model.setIronMineCostMeat(meat);
                model.setIronMineCostIron(iron);
                model.setIronMineDuration(duration);
            }
            case farm -> {
                model.setFarmCostWood(wood);
                model.setFarmCostMeat(meat);
                model.setFarmCostIron(iron);
                model.setFarmDuration(duration);
            }
            case warehouse -> {
                model.setWarehouseCostWood(wood);
                model.setWarehouseCostMeat(meat);
                model.setWarehouseCostIron(iron);
                model.setWarehouseDuration(duration);
            }
            case wall -> {
                model.setWallCostWood(wood);
                model.setWallCostMeat(meat);
                model.setWallCostIron(iron);
                model.setWallDuration(duration);
            }
        }
    }

    public List<ConstructionModel> getConstructionQueue(Long villageId) {
        if (!villageRepository.existsById(villageId)) {
            throw new RuntimeException("Köy bulunamadı!");
        }
        List<BuildingConstructionEntity> constructions = constructionRepository.findByVillageIdOrderByCompletionTimeAsc(villageId);
        return constructions.stream()
                .map(constructionConverter::toModel)
                .toList();
    }

    // Helper: Seviye Okuma
    private int getBuildingLevel(VillageBuildingsEntity buildings, BuildingType type) {
        return switch (type) {
            case headquarters -> buildings.getHeadquarters();
            case farm -> buildings.getFarm();
            case warehouse -> buildings.getWarehouse();
            case wall -> buildings.getWall();
            case timberCamp -> buildings.getTimberCamp();
            case meatPlant -> buildings.getMeatPlant();
            case ironMine -> buildings.getIronMine();
            case barracks -> buildings.getBarracks();
            case stable -> buildings.getStable();
            case workshop -> buildings.getWorkshop();
            case academy -> buildings.getAcademy();
            case smithy -> buildings.getSmithy();
            case market -> buildings.getMarket();
            default -> 0;
        };
    }

    // Helper: Seviye Yazma (Worker metodu için gerekli)
    private void applyBuildingUpgrade(VillageBuildingsEntity buildings, BuildingType type, int level) {
        switch (type) {
            case headquarters -> buildings.setHeadquarters(level);
            case farm -> buildings.setFarm(level);
            case warehouse -> buildings.setWarehouse(level);
            case wall -> buildings.setWall(level);
            case timberCamp -> buildings.setTimberCamp(level);
            case meatPlant -> buildings.setMeatPlant(level);
            case ironMine -> buildings.setIronMine(level);
            case barracks -> buildings.setBarracks(level);
            case stable -> buildings.setStable(level);
            case workshop -> buildings.setWorkshop(level);
            case academy -> buildings.setAcademy(level);
            case smithy -> buildings.setSmithy(level);
            case market -> buildings.setMarket(level);
        }
    }

    // --- INITIALIZERS (CreateFirstVillage için) ---

    private void createInitialBuildings(VillageEntity village) {
        VillageBuildingsEntity buildings = new VillageBuildingsEntity();
        buildings.setVillage(village);
        buildings.setHeadquarters(1);
        buildings.setFarm(1);
        buildings.setWarehouse(1);
        villageBuildingsRepository.save(buildings);
        village.setBuildings(buildings);
    }

    private void createInitialResources(VillageEntity village) {
        VillageResourcesEntity resources = new VillageResourcesEntity();
        resources.setVillage(village);
        resources.setWoodAmount(500);
        resources.setMeatAmount(500);
        resources.setIronAmount(500);
        resources.setLastUpdated(LocalDateTime.now());
        villageResourcesRepository.save(resources);
        village.setResources(resources);
    }

    private void createInitialTroops(VillageEntity village) {
        VillageTroopsEntity troops = new VillageTroopsEntity();
        troops.setVillage(village);
        villageTroopsRepository.save(troops);
        village.setTroops(troops);
    }

    private void createInitialResearches(VillageEntity village) {
        VillageResearchesEntity researches = new VillageResearchesEntity();
        researches.setVillage(village);
        villageResearchesRepository.save(researches);
        village.setResearches(researches);
    }

    private int[] findCoordinatesByRegion(RegionDirection direction) {
        Random random = new Random();
        int xMin = 0, xMax = MAP_SIZE;
        int yMin = 0, yMax = MAP_SIZE;

        switch (direction) {
            case NORTH -> yMax = MAP_SIZE / 2;
            case SOUTH -> yMin = MAP_SIZE / 2;
            case WEST -> xMax = MAP_SIZE / 2;
            case EAST -> xMin = MAP_SIZE / 2;
        }

        int x, y;
        boolean exists;
        int attempt = 0;

        do {
            x = random.nextInt(xMax - xMin) + xMin;
            y = random.nextInt(yMax - yMin) + yMin;
            exists = villageRepository.findByXcoordAndYcoord(x, y).isPresent();
            attempt++;
            if (attempt > 1000) {
                x = random.nextInt(MAP_SIZE);
                y = random.nextInt(MAP_SIZE);
            }
        } while (exists);

        return new int[]{x, y};
    }

    @Override
    @Transactional
    public List<VillageModel> getVillagesByPlayerId(Long playerId) {
        var optPlayer = playerRepository.findById(playerId);
        List<VillageEntity> villages;
        if (optPlayer.isPresent()) {
            villages = villageRepository.findByPlayer(optPlayer.get());
        } else {
            throw new RuntimeException("Player bulunamadı!");
        }
        return villages.stream().map(village -> {
            resourceService.calculateAndUpdateResources(village);
            return villageConverter.toModel(village);
        }).toList();
    }
}