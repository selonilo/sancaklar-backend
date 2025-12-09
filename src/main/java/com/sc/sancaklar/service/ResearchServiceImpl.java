package com.sc.sancaklar.service;

import com.sc.sancaklar.model.data.GameCalculator;
import com.sc.sancaklar.model.dto.ResearchQueueModel;
import com.sc.sancaklar.model.dto.VillageResearchesModel;
import com.sc.sancaklar.model.entity.*;
import com.sc.sancaklar.model.enums.UnitType;
import com.sc.sancaklar.model.mapper.ResearchConverter;
import com.sc.sancaklar.repository.ResearchQueueRepository;
import com.sc.sancaklar.repository.VillageRepository;
import com.sc.sancaklar.repository.VillageResearchesRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResearchServiceImpl implements ResearchService {

    private final VillageRepository villageRepository;
    private final ResearchQueueRepository researchQueueRepository;
    private final ResourceService resourceService;
    private final ResearchConverter researchConverter;
    private final JobScheduler jobScheduler;
    private final VillageResearchesRepository villageResearchesRepository;

    @Transactional
    public void startResearch(Long villageId, UnitType unitType) {
        VillageEntity village = villageRepository.findById(villageId)
                .orElseThrow(() -> new RuntimeException("Köy bulunamadı!"));

        resourceService.calculateAndUpdateResources(village);

        VillageBuildingsEntity buildings = village.getBuildings();
        VillageResearchesEntity researches = village.getResearches();
        VillageResourcesEntity resources = village.getResources();

        // 1. Zaten araştırılmış mı?
        if (isResearched(researches, unitType)) {
            throw new RuntimeException("Bu teknoloji zaten açık!");
        }

        // 2. Şu an kuyrukta mı?
        if (researchQueueRepository.existsByVillageAndUnitType(village, unitType)) {
            throw new RuntimeException("Bu teknoloji şu an zaten araştırılıyor!");
        }

        // 3. Bina gereksinimleri karşılıyor mu? (Örn: Demirci Lv 15 lazım)
        if (!GameCalculator.isResearchRequirementsMet(unitType, buildings)) {
            throw new RuntimeException("Bina gereksinimleri karşılanmıyor! Önce binaları geliştir.");
        }

        // 4. Kaynak Yeterli mi?
        int[] costs = GameCalculator.getResearchCost(unitType);
        if (resources.getWoodAmount() < costs[0] || resources.getMeatAmount() < costs[1] || resources.getIronAmount() < costs[2]) {
            throw new RuntimeException("Yetersiz Kaynak!");
        }

        // 5. Kaynakları Düş
        resources.setWoodAmount(resources.getWoodAmount() - costs[0]);
        resources.setMeatAmount(resources.getMeatAmount() - costs[1]);
        resources.setIronAmount(resources.getIronAmount() - costs[2]);

        // 6. Kuyruğa Ekle
        int worldSpeed = village.getPlayer().getWorld().getSpeed();
        long duration = GameCalculator.getResearchTime(unitType, buildings.getSmithy(), worldSpeed);

        LocalDateTime completionTime = LocalDateTime.now().plusSeconds(duration);

        ResearchQueueEntity research = new ResearchQueueEntity();
        research.setVillage(village);
        research.setUnitType(unitType);
        research.setStartTime(LocalDateTime.now());
        research.setCompletionTime(completionTime);

        researchQueueRepository.save(research);

        // 10. JOBRUNR ZAMANLAMASI
        OffsetDateTime jobTime = completionTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
        final Long researchTaskId = research.getId();

        // Worker metodumuzu çağırıyoruz
        var scheduledJobId = jobScheduler.schedule(jobTime,
                () -> completeResearchJob(researchTaskId)
        );

        // 11. Job ID'yi kaydet (İptal etmek istersen lazım olur)
        research.setJobId(scheduledJobId.asUUID().toString());
    }

    // Dashboard'da ne iş yaptığı belli olsun
    @Job(name = "Research Completion")
    @Transactional
    public void completeResearchJob(Long researchTaskId) {
        // 1. Görevi bul
        ResearchQueueEntity task = researchQueueRepository.findById(researchTaskId).orElse(null);

        if (task == null) {
            // Görev silinmişse (iptal edilmişse) işlem yapma
            return;
        }

        VillageEntity village = task.getVillage();
        VillageResearchesEntity researches = village.getResearches();

        // 2. İlgili birimi '1' (Açık) yap
        // Senin switch-case mantığını buraya taşıyoruz
        switch (task.getUnitType()) {
            // --- PİYADELER ---
            case SPEARMAN: researches.setSpearmen(1); break;
            case SWORDSMAN: researches.setSwordsmen(1); break;
            case AXEMAN: researches.setAxemen(1); break;
            case ARCHER: researches.setArchers(1); break;

            // --- ATLILAR VE CASUS ---
            case SCOUT: researches.setScouts(1); break;
            case LIGHT_CAVALRY: researches.setLightCavalry(1); break;
            case HEAVY_CAVALRY: researches.setHeavyCavalry(1); break;

            // --- KUŞATMA VE ÖZEL ---
            case RAM: researches.setRams(1); break;
            case CATAPULT: researches.setCatapults(1); break;
            case CONQUEROR: researches.setConquerors(1); break; // Sancaklar/Misyoner
        }

        // Değişiklikleri kaydet
        villageResearchesRepository.save(researches);

        // 3. (Opsiyonel) Bildirim veya Log
        System.out.println("Araştırma tamamlandı: Köy " + village.getId() + " - " + task.getUnitType());

        // 4. Görevi Kuyruktan Sil
        researchQueueRepository.delete(task);
    }

    @Override
    public boolean isResearched(VillageResearchesEntity r, UnitType type) {
        return switch (type) {
            // --- PİYADELER ---
            case SPEARMAN -> r.getSpearmen() > 0;
            case SWORDSMAN -> r.getSwordsmen() > 0;
            case AXEMAN -> r.getAxemen() > 0;
            case ARCHER -> r.getArchers() > 0;

            // --- ATLILAR VE CASUS ---
            case SCOUT -> r.getScouts() > 0;
            case LIGHT_CAVALRY -> r.getLightCavalry() > 0;
            case HEAVY_CAVALRY -> r.getHeavyCavalry() > 0;

            // --- KUŞATMA SİLAHLARI ---
            case RAM -> r.getRams() > 0;
            case CATAPULT -> r.getCatapults() > 0;

            // --- ÖZEL BİRİM ---
            case CONQUEROR -> r.getConquerors() > 0;
            default -> false;
        };
    }

    // 1. Mevcut Araştırma Seviyelerini Getir
    public VillageResearchesModel getResearches(Long villageId) {
        VillageEntity village = villageRepository.findById(villageId)
                .orElseThrow(() -> new RuntimeException("Köy bulunamadı!"));

        // Köyün researches tablosunu modele çevirip dön
        return researchConverter.toModel(village.getResearches());
    }

    // 2. Araştırma Kuyruğunu Getir
    public List<ResearchQueueModel> getResearchQueue(Long villageId) {
        // O köydeki kuyruğu çek
        List<ResearchQueueEntity> queue = researchQueueRepository.findByVillageIdOrderByCompletionTimeAsc(villageId);

        // Listeyi modele map'le
        return queue.stream()
                .map(researchConverter::toQueueModel)
                .toList();
    }
}