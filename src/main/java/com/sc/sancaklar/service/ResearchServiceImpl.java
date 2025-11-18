package com.sc.sancaklar.service;

import com.sc.sancaklar.model.data.GameCalculator;
import com.sc.sancaklar.model.dto.ResearchQueueModel;
import com.sc.sancaklar.model.dto.VillageResearchesModel;
import com.sc.sancaklar.model.entity.*;
import com.sc.sancaklar.model.enums.UnitType;
import com.sc.sancaklar.model.mapper.ResearchConverter;
import com.sc.sancaklar.repository.ResearchQueueRepository;
import com.sc.sancaklar.repository.VillageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResearchServiceImpl implements ResearchService {

    private final VillageRepository villageRepository;
    private final ResearchQueueRepository researchQueueRepository;
    private final ResourceService resourceService;
    private final ResearchConverter researchConverter;

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

        ResearchQueueEntity research = new ResearchQueueEntity();
        research.setVillage(village);
        research.setUnitType(unitType);
        research.setStartTime(LocalDateTime.now());
        research.setCompletionTime(LocalDateTime.now().plusSeconds(duration));

        researchQueueRepository.save(research);
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