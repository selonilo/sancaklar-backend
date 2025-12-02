package com.sc.sancaklar.service;

import com.sc.sancaklar.model.data.GameCalculator;
import com.sc.sancaklar.model.dto.RecruitRequest;
import com.sc.sancaklar.model.dto.UnitRecruitmentModel;
import com.sc.sancaklar.model.entity.UnitRecruitmentEntity;
import com.sc.sancaklar.model.entity.VillageBuildingsEntity;
import com.sc.sancaklar.model.entity.VillageEntity;
import com.sc.sancaklar.model.entity.VillageResourcesEntity;
import com.sc.sancaklar.model.enums.BuildingType;
import com.sc.sancaklar.model.mapper.RecruitmentConverter;
import com.sc.sancaklar.repository.UnitRecruitmentRepository;
import com.sc.sancaklar.repository.VillageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruitmentServiceImpl implements RecruitmentService{

    private final VillageRepository villageRepository;
    private final UnitRecruitmentRepository recruitmentRepository;
    private final ResourceService resourceService;
    private final ResearchService researchService; // Araştırma kontrolü için
    private final RecruitmentConverter recruitmentConverter;

    @Override
    @Transactional
    public void recruitUnits(RecruitRequest request) {
        // 1. Validasyonlar
        if (request.getAmount() <= 0) throw new RuntimeException("Miktar 0'dan büyük olmalı!");

        VillageEntity village = villageRepository.findById(request.getVillageId())
                .orElseThrow(() -> new RuntimeException("Köy bulunamadı!"));

        resourceService.calculateAndUpdateResources(village);

        VillageResourcesEntity resources = village.getResources();
        VillageBuildingsEntity buildings = village.getBuildings();

        // 2. Araştırma Kontrolü (Daha önce yazdığın metod)
        // Bu metod ResearchService içinde 'public' olmalı
        if (!researchService.isResearched(village.getResearches(), request.getUnitType())) {
            throw new RuntimeException("Bu birim henüz araştırılmamış!");
        }

        // 3. Kaynak ve Nüfus Maliyeti Hesapla
        int[] unitCost = GameCalculator.getUnitCost(request.getUnitType());
        int totalWood = unitCost[0] * request.getAmount();
        int totalMeat = unitCost[1] * request.getAmount();
        int totalIron = unitCost[2] * request.getAmount();

        // Çiftlik Limiti Kontrolü (Basitleştirilmiş)
        // Gerçekte: Mevcut Askerler + Kuyruktakiler + Yeni İstek <= Çiftlik Kapasitesi
        int unitPop = GameCalculator.getUnitPopulation(request.getUnitType());
        int requiredPop = unitPop * request.getAmount();

        // TODO: Mevcut nüfus hesaplama metodu eklenecek (calculateUsedPopulation)
        // Şimdilik kaynak yetiyorsa bas diyelim, farm kontrolünü ayrıca ekleriz.

        if (resources.getWoodAmount() < totalWood ||
                resources.getMeatAmount() < totalMeat ||
                resources.getIronAmount() < totalIron) {
            throw new RuntimeException("Yetersiz Kaynak!");
        }

        // 4. Kaynakları Düş
        resources.setWoodAmount(resources.getWoodAmount() - totalWood);
        resources.setMeatAmount(resources.getMeatAmount() - totalMeat);
        resources.setIronAmount(resources.getIronAmount() - totalIron);

        // 5. Süre Hesapla
        int worldSpeed = village.getPlayer().getWorld().getSpeed();
        BuildingType productionBuilding = GameCalculator.getProductionBuilding(request.getUnitType());

        // İlgili binanın seviyesini bul (Switch-case helper kullan)
        int buildingLevel = getBuildingLevel(buildings, productionBuilding);

        if (buildingLevel == 0) throw new RuntimeException("Üretim binası (Kışla/Ahır) kurulu değil!");

        long oneUnitSeconds = GameCalculator.getUnitProductionTime(request.getUnitType(), buildingLevel, worldSpeed);
        long totalSeconds = oneUnitSeconds * request.getAmount();

        // 6. Kuyruk Mantığı (Queue)
        // Eğer Kışlada zaten üretim varsa, bu emir onun bitişine eklenmeli.
        // Ancak Ahır üretimi Kışlayı beklemez. Paraleldir.

        LocalDateTime startTime = LocalDateTime.now();

        // Köydeki tüm kuyruğu çekip, aynı tip binada olan en son işi buluyoruz
        List<UnitRecruitmentEntity> queue = recruitmentRepository.findByVillageIdOrderByCompletionTimeDesc(village.getId());

        for (UnitRecruitmentEntity item : queue) {
            BuildingType itemBuilding = GameCalculator.getProductionBuilding(item.getUnitType());
            if (itemBuilding == productionBuilding) {
                // Aynı binada (Örn: Kışla) başka iş var, onun bitişini bekle
                if (item.getCompletionTime().isAfter(startTime)) {
                    startTime = item.getCompletionTime();
                }
                break; // En son biteni bulduk (OrderByDesc sayesinde), döngüden çık
            }
        }

        LocalDateTime completionTime = startTime.plusSeconds(totalSeconds);

        // 7. Kaydet
        UnitRecruitmentEntity recruitment = new UnitRecruitmentEntity();
        recruitment.setVillage(village);
        recruitment.setUnitType(request.getUnitType());
        recruitment.setQuantity(request.getAmount());
        recruitment.setSecondsPerUnit((int) oneUnitSeconds); // Bilgi amaçlı
        recruitment.setCompletionTime(completionTime);
        // startTime entity'de yoksa ekleyebilirsin veya completion - duration yapabilirsin

        recruitmentRepository.save(recruitment);
    }

    // Helper: ResearchService'deki private metodu buraya kopyalayabilirsin
    // veya ResearchService'dekini public yapıp çağırabilirsin.

    private int getBuildingLevel(VillageBuildingsEntity b, BuildingType type) {
        return switch (type) {
            case barracks -> b.getBarracks();
            case stable -> b.getStable();
            case workshop -> b.getWorkshop();
            case academy -> b.getAcademy();
            default -> 0;
        };
    }

    public List<UnitRecruitmentModel> getRecruitmentQueue(Long villageId, BuildingType buildingType) {
        // 1. Köydeki TÜM üretimleri çek
        List<UnitRecruitmentEntity> allRecruitments = recruitmentRepository.findByVillageIdOrderByCompletionTimeAsc(villageId);

        // 2. Sadece istenen binada üretilenleri filtrele ve Model'e çevir
        return allRecruitments.stream()
                .filter(recruitment -> {
                    // Bu asker hangi binada üretiliyor?
                    BuildingType productionPlace = GameCalculator.getProductionBuilding(recruitment.getUnitType());
                    // İstenen bina ile eşleşiyor mu?
                    return productionPlace == buildingType;
                })
                .map(recruitmentConverter::toModel)
                .toList();
    }
}