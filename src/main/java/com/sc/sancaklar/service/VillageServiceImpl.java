package com.sc.sancaklar.service;

import com.sc.sancaklar.model.data.GameCalculator;
import com.sc.sancaklar.model.dto.ConstructionModel;
import com.sc.sancaklar.model.dto.PlayerModel;
import com.sc.sancaklar.model.dto.UpgradeBuildingRequest;
import com.sc.sancaklar.model.dto.village.VillageModel;
import com.sc.sancaklar.model.entity.*;
import com.sc.sancaklar.model.enums.BuildingType;
import com.sc.sancaklar.model.enums.RegionDirection;
import com.sc.sancaklar.model.mapper.ConstructionConverter;
import com.sc.sancaklar.model.mapper.VillageConverter;
import com.sc.sancaklar.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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

    private static final int MAP_SIZE = 1000;

    @Transactional
    @Override
    public VillageModel getVillageById(Long villageId) {
        VillageEntity village = villageRepository.findById(villageId)
                .orElseThrow(() -> new RuntimeException("Köy bulunamadı! ID: " + villageId));

        resourceService.calculateAndUpdateResources(village);

        return villageConverter.toModel(village);
    }

    @Transactional
    @Override
    public VillageModel createFirstVillage(PlayerModel playerModel, RegionDirection direction) {

        PlayerEntity playerEntity = playerRepository.findById(playerModel.getId())
                .orElseThrow(() -> new RuntimeException("Oyuncu bulunamadı!"));

        // 1. Seçilen yöne göre koordinat bul
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

    // --- PRIVATE HELPERS ---

    private void createInitialBuildings(VillageEntity village) {
        VillageBuildingsEntity buildings = new VillageBuildingsEntity();
        buildings.setVillage(village);
        buildings.setHeadquarters(1);
        buildings.setFarm(1);
        buildings.setWarehouse(1);

        VillageBuildingsEntity saved = villageBuildingsRepository.save(buildings);
        village.setBuildings(saved);
    }

    private void createInitialResources(VillageEntity village) {
        VillageResourcesEntity resources = new VillageResourcesEntity();
        resources.setVillage(village);
        resources.setWoodAmount(500);
        resources.setMeatAmount(500);
        resources.setIronAmount(500);
        resources.setLastUpdated(LocalDateTime.now());

        VillageResourcesEntity saved = villageResourcesRepository.save(resources);
        village.setResources(saved);
    }

    private void createInitialTroops(VillageEntity village) {
        VillageTroopsEntity troops = new VillageTroopsEntity();
        troops.setVillage(village);

        VillageTroopsEntity saved = villageTroopsRepository.save(troops);
        village.setTroops(saved);
    }

    private void createInitialResearches(VillageEntity village) {
        VillageResearchesEntity researches = new VillageResearchesEntity();
        researches.setVillage(village);

        VillageResearchesEntity saved = villageResearchesRepository.save(researches);

        village.setResearches(saved);
    }

    private int[] findCoordinatesByRegion(RegionDirection direction) {
        Random random = new Random();

        // Varsayılan olarak tüm harita (RANDOM durumu için)
        int xMin = 0, xMax = MAP_SIZE;
        int yMin = 0, yMax = MAP_SIZE;

        switch (direction) {
            case NORTH: // KUZEY (Üst Yarım)
                // X değişmez (0-1000), Y üstte sınırlandırılır (0-500)
                yMax = MAP_SIZE / 2;
                break;

            case SOUTH: // GÜNEY (Alt Yarım)
                // X değişmez, Y altta sınırlandırılır (500-1000)
                yMin = MAP_SIZE / 2;
                break;

            case WEST:  // BATI (Sol Yarım)
                // X solda sınırlandırılır (0-500), Y değişmez
                xMax = MAP_SIZE / 2;
                break;

            case EAST:  // DOĞU (Sağ Yarım)
                // X sağda sınırlandırılır (500-1000), Y değişmez
                xMin = MAP_SIZE / 2;
                break;

            case RANDOM:
            default:
                // Tüm harita serbest
                break;
        }

        int x, y;
        boolean exists;
        int attempt = 0;

        do {
            // Formül: random.nextInt(max - min) + min
            // Örn Kuzey için: x(0-1000), y(0-500) arası üretir
            x = random.nextInt(xMax - xMin) + xMin;
            y = random.nextInt(yMax - yMin) + yMin;

            exists = villageRepository.findByXcoordAndYcoord(x, y).isPresent();

            attempt++;
            // Eğer bölge çok doluysa ve yer bulamazsa, oyuncuyu mağdur etmemek için
            // son çare olarak haritanın herhangi bir yerine atıyoruz.
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

        // 2. Her bir köy için kaynakları hesapla ve modele çevir
        return villages.stream().map(village -> {
            // Kaynakları güncelle (Lazy Update)
            resourceService.calculateAndUpdateResources(village);
            // Modele çevir
            return villageConverter.toModel(village);
        }).toList();
    }

    @Transactional
    public void upgradeBuilding(UpgradeBuildingRequest request) {
        // 1. Köyü Bul
        VillageEntity village = villageRepository.findById(request.getVillageId())
                .orElseThrow(() -> new RuntimeException("Köy bulunamadı!"));

        // 2. Kaynakları GÜNCELLE (Çok önemli, yoksa parası olduğu halde yok sanabilir)
        resourceService.calculateAndUpdateResources(village);

        VillageResourcesEntity resources = village.getResources();
        VillageBuildingsEntity buildings = village.getBuildings();

        // 3. Binanın Mevcut Seviyesini Bul
        int currentLevel = getBuildingLevel(buildings, request.getBuildingType());

        // 3.1. Kuyrukta bekleyen bu binadan başka emirler varsa, hedef seviyeyi artır
        // Örn: Şu an Lv5. Kuyrukta "Lv6 Yap" emri var. Yeni emir Lv7 olmalı.
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
            throw new RuntimeException("Yetersiz Kaynak! (Odun: " + costWood + ", Et: " + costMeat + ", Demir: " + costIron + ")");
        }

        // 6. Kaynakları Düş
        resources.setWoodAmount(resources.getWoodAmount() - costWood);
        resources.setMeatAmount(resources.getMeatAmount() - costMeat);
        resources.setIronAmount(resources.getIronAmount() - costIron);

        // Not: ResourceService içinde save olduğu için burada resources repository çağırmaya gerek yok,
        // Transactional anotasyonu metod sonunda entity'deki değişikliği algılayıp update atar.

        // 7. İnşaat Süresini Hesapla
        int hqLevel = buildings.getHeadquarters();
        int worldSpeed = village.getPlayer().getWorld().getSpeed();
        long durationSeconds = GameCalculator.calculateConstructionTimeInSeconds(request.getBuildingType(), targetLevel, hqLevel, worldSpeed);

        // 8. Başlangıç ve Bitiş Zamanını Ayarla (Kuyruk Mantığı)
        LocalDateTime startTime = LocalDateTime.now();

        // Eğer kuyrukta bekleyen başka bir iş varsa, onun bitişini bekle
        List<BuildingConstructionEntity> constructionQueue = constructionRepository.findByVillageOrderByCompletionTimeDesc(village);
        if (!constructionQueue.isEmpty()) {
            // En son bitecek olan işin bitiş zamanı, bizim başlangıç zamanımızdır.
            startTime = constructionQueue.get(0).getCompletionTime();
        }

        LocalDateTime completionTime = startTime.plusSeconds(durationSeconds);

        // 9. Emri Kaydet
        BuildingConstructionEntity construction = new BuildingConstructionEntity();
        construction.setVillage(village);
        construction.setBuildingType(request.getBuildingType());
        construction.setTargetLevel(targetLevel);
        construction.setStartTime(startTime);
        construction.setCompletionTime(completionTime);

        constructionRepository.save(construction);
    }

    // Helper: Reflection veya Switch-Case ile level çekme
    private int getBuildingLevel(VillageBuildingsEntity buildings, BuildingType type) {
        return switch (type) {
            // --- ANA YÖNETİM ---
            case HEADQUARTERS -> buildings.getHeadquarters();
            case FARM -> buildings.getFarm();
            case WAREHOUSE -> buildings.getWarehouse();
            case WALL -> buildings.getWall();

            // --- KAYNAKLAR ---
            case TIMBER_CAMP -> buildings.getTimberCamp();
            case MEAT_PLANT ->   // Enum adı: MEAT_PLANT, Entity alanı: meatProduction
                    buildings.getMeatProduction();
            case IRON_MINE -> buildings.getIronMine();

            // --- ASKERİ VE TEKNOLOJİ ---
            case BARRACKS -> buildings.getBarracks();
            case STABLE -> buildings.getStable();
            case WORKSHOP -> buildings.getWorkshop();
            case ACADEMY -> buildings.getAcademy();
            case SMITHY -> buildings.getSmithy();

            // --- TİCARET ---
            case MARKET -> buildings.getMarket();
            default -> 0;
        };
    }

    public List<ConstructionModel> getConstructionQueue(Long villageId) {
        // 1. Köy var mı kontrolü (Opsiyonel ama güvenli)
        if (!villageRepository.existsById(villageId)) {
            throw new RuntimeException("Köy bulunamadı!");
        }

        // 2. O köye ait inşaatları, BİTİŞ süresine göre (önce bitecek olan üstte) getir
        List<BuildingConstructionEntity> constructions = constructionRepository.findByVillageIdOrderByCompletionTimeAsc(villageId);

        // 3. Entity listesini Model listesine çevir
        return constructions.stream()
                .map(constructionConverter::toModel)
                .toList();
    }
}