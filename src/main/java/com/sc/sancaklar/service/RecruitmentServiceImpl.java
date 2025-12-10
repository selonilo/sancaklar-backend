package com.sc.sancaklar.service;

import com.sc.sancaklar.model.data.GameCalculator;
import com.sc.sancaklar.model.dto.RecruitRequest;
import com.sc.sancaklar.model.dto.UnitRecruitmentModel;
import com.sc.sancaklar.model.entity.*;
import com.sc.sancaklar.model.enums.BuildingType;
import com.sc.sancaklar.model.mapper.RecruitmentConverter;
import com.sc.sancaklar.repository.UnitRecruitmentRepository;
import com.sc.sancaklar.repository.VillageRepository;
import com.sc.sancaklar.repository.VillageTroopsRepository;
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

@Service
@RequiredArgsConstructor
public class RecruitmentServiceImpl implements RecruitmentService{

    private final VillageRepository villageRepository;
    private final UnitRecruitmentRepository recruitmentRepository;
    private final ResourceService resourceService;
    private final ResearchService researchService; // Araştırma kontrolü için
    private final RecruitmentConverter recruitmentConverter;
    private final VillageTroopsRepository villageTroopsRepository;
    private final UnitRecruitmentRepository unitRecruitmentRepository;
    private final JobScheduler jobScheduler;

    @Transactional
    @Override
    public void recruitUnits(RecruitRequest request) {
        // --- 1. Validasyonlar ---
        if (request.getAmount() <= 0) {
            throw new RuntimeException("Miktar 0'dan büyük olmalı!");
        }

        VillageEntity village = villageRepository.findById(request.getVillageId())
                .orElseThrow(() -> new RuntimeException("Köy bulunamadı!"));

        // --- 2. Kaynak Hesaplama ve Kontroller ---
        resourceService.calculateAndUpdateResources(village);
        VillageResourcesEntity resources = village.getResources();
        VillageBuildingsEntity buildings = village.getBuildings();

        // Birim Araştırılmış mı?
        if (!researchService.isResearched(village.getResearches(), request.getUnitType())) {
            throw new RuntimeException("Bu birim henüz araştırılmamış!");
        }

        // Üretim Binası (Kışla/Ahır) Kurulu mu?
        BuildingType productionBuilding = GameCalculator.getProductionBuilding(request.getUnitType());
        int buildingLevel = getBuildingLevel(buildings, productionBuilding);
        if (buildingLevel == 0) {
            throw new RuntimeException("Üretim binası (" + productionBuilding + ") kurulu değil!");
        }

        // --- 3. Maliyet Hesaplama ---
        int[] unitCost = GameCalculator.getUnitCost(request.getUnitType());
        int costWood = unitCost[0] * request.getAmount();
        int costMeat = unitCost[1] * request.getAmount();
        int costIron = unitCost[2] * request.getAmount();

        // Kaynak Yeterli mi?
        if (resources.getWoodAmount() < costWood ||
                resources.getMeatAmount() < costMeat ||
                resources.getIronAmount() < costIron) {
            throw new RuntimeException("Yetersiz Kaynak!");
        }

        // Nüfus Yeterli mi? (Basit kontrol)
        int popPerUnit = GameCalculator.getUnitPopulation(request.getUnitType());
        //çiftlik kontrollerini düzenleyecez.
        /*int totalPopNeeded = popPerUnit * request.getAmount();
        // Buraya calculateUsedPopulation(village) metodu ile detaylı kontrol eklenmeli
        if (resources.getPopulation() + totalPopNeeded > resources.getMaxPopulation()) {
            throw new RuntimeException("Çiftlikte yer yok!");
        }*/

        // --- 4. Ödeme Alma ---
        resources.setWoodAmount(resources.getWoodAmount() - costWood);
        resources.setMeatAmount(resources.getMeatAmount() - costMeat);
        resources.setIronAmount(resources.getIronAmount() - costIron);
        //resources.setPopulation(resources.getPopulation() + totalPopNeeded); // Nüfusu hemen düşüyoruz

        // --- 5. Süre Hesaplama ---
        int worldSpeed = village.getPlayer().getWorld().getSpeed();
        // Bir askerin üretim süresi (saniye)
        long oneUnitSeconds = GameCalculator.getUnitProductionTime(request.getUnitType(), buildingLevel, worldSpeed);
        // Toplam süre
        long totalDurationSeconds = oneUnitSeconds * request.getAmount();

        // --- 6. KUYRUK MANTIĞI (Queue Logic) ---
        // Şimdiki zamanı alıyoruz
        LocalDateTime effectiveStartTime = LocalDateTime.now();

        // Veritabanından bu köyde ve BU BİNADA (Örn: Sadece Kışla) bekleyen son işi bul
        Optional<UnitRecruitmentEntity> lastJob = recruitmentRepository
                .findFirstByVillageAndBuildingTypeOrderByCompletionTimeDesc(village, productionBuilding);

        if (lastJob.isPresent()) {
            LocalDateTime lastEndTime = lastJob.get().getCompletionTime();
            // Eğer kuyruktaki son iş henüz bitmediyse, bizim işimiz onun bittiği an başlar
            if (lastEndTime.isAfter(effectiveStartTime)) {
                effectiveStartTime = lastEndTime;
            }
        }

        // Bitiş zamanı = (Efektif Başlangıç) + (Toplam Süre)
        LocalDateTime completionTime = effectiveStartTime.plusSeconds(totalDurationSeconds);

        // --- 7. Entity Oluşturma ve Kaydetme ---
        UnitRecruitmentEntity recruitment = new UnitRecruitmentEntity();
        recruitment.setVillage(village);
        recruitment.setUnitType(request.getUnitType());
        recruitment.setQuantity(request.getAmount());
        recruitment.setProducedAmount(0); // Henüz hiç üretilmedi
        recruitment.setBuildingType(productionBuilding); // EnumType.ORDINAL olarak kaydedilecek
        recruitment.setSecondsPerUnit((int) oneUnitSeconds);
        recruitment.setCompletionTime(completionTime);

        // Önce DB'ye kaydedip ID alalım
        recruitment = recruitmentRepository.save(recruitment);

        // --- 8. JobRunr Zamanlaması ---
        // İşlem tam bittiği an çalışacak bir job ayarlıyoruz
        OffsetDateTime jobTime = completionTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
        final Long recruitmentId = recruitment.getId();

        var scheduledJobId = jobScheduler.schedule(jobTime,
                () -> completeRecruitmentJob(recruitmentId)
        );

        // Job ID'yi güncelle
        recruitment.setJobId(scheduledJobId.asUUID().toString());

        System.out.println("Asker üretimi başlandı: Köy " + village.getId());
    }

    // --- WORKER METHOD ---

    @Job(name = "Complete Unit Recruitment") // Dashboard'da görünecek isim
    @Transactional
    public void completeRecruitmentJob(Long recruitmentId) {
        // 1. Görevi ID ile bul
        UnitRecruitmentEntity task = recruitmentRepository.findById(recruitmentId).orElse(null);

        // Eğer görev silinmişse veya iptal edilmişse işlem yapma
        if (task == null) {
            return;
        }

        VillageEntity village = task.getVillage();

        // 2. Askerleri Köye Ekle (Senin switch-case mantığın)
        VillageTroopsEntity troops = village.getTroops();

        // Eğer troops tablosu null gelirse (ilk kez asker üretiliyorsa) oluştur
        if (troops == null) {
            troops = new VillageTroopsEntity();
            troops.setVillage(village);
            // Diğer fieldları 0 set edebilirsin veya entity default değerlerin vardır
        }

        int amount = task.getQuantity();

        switch (task.getUnitType()) {
            case SPEARMAN -> troops.setSpearman(troops.getSpearman() + amount);
            case SWORDSMAN -> troops.setSwordsman(troops.getSwordsman() + amount);
            case AXEMAN -> troops.setAxeman(troops.getAxeman() + amount);
            case ARCHER -> troops.setArcher(troops.getArcher() + amount);

            case SCOUT -> troops.setScout(troops.getScout() + amount);
            case LIGHT_CAVALRY -> troops.setLightCavalry(troops.getLightCavalry() + amount);
            case HEAVY_CAVALRY -> troops.setHeavyCavalry(troops.getHeavyCavalry() + amount);

            case RAM -> troops.setRam(troops.getRam() + amount);
            case CATAPULT -> troops.setCatapult(troops.getCatapult() + amount);

            case CONQUEROR -> troops.setConqueror(troops.getConqueror() + amount);
        }

        // 3. Güncel Asker Sayısını Kaydet
        villageTroopsRepository.save(troops);

        // 4. Görevi Kuyruktan Sil (İşlem tamamlandı)
        unitRecruitmentRepository.delete(task);

        System.out.println("Asker üretimi tamamlandı: Köy " + village.getId() + " - " + task.getUnitType());
    }

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