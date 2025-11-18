package com.sc.sancaklar.model.mapper;

import com.sc.sancaklar.model.dto.ResearchQueueModel;
import com.sc.sancaklar.model.dto.VillageResearchesModel;
import com.sc.sancaklar.model.entity.ResearchQueueEntity;
import com.sc.sancaklar.model.entity.VillageResearchesEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class ResearchConverter {

    // Entity -> Durum Modeli
    public VillageResearchesModel toModel(VillageResearchesEntity entity) {
        if (entity == null) return new VillageResearchesModel();

        VillageResearchesModel model = new VillageResearchesModel();
        model.setSpearmen(entity.getSpearmen());
        model.setSwordsmen(entity.getSwordsmen());
        model.setAxemen(entity.getAxemen());
        model.setArchers(entity.getArchers());
        model.setScouts(entity.getScouts());
        model.setLightCavalry(entity.getLightCavalry());
        model.setHeavyCavalry(entity.getHeavyCavalry());
        model.setRams(entity.getRams());
        model.setCatapults(entity.getCatapults());
        model.setConquerors(entity.getConquerors());

        return model;
    }

    // Entity -> Kuyruk Modeli
    public ResearchQueueModel toQueueModel(ResearchQueueEntity entity) {
        if (entity == null) return null;

        ResearchQueueModel model = new ResearchQueueModel();
        model.setId(entity.getId());
        model.setUnitType(entity.getUnitType());
        model.setStartTime(entity.getStartTime());
        model.setCompletionTime(entity.getCompletionTime());

        // Kalan süre hesabı
        long seconds = Duration.between(LocalDateTime.now(), entity.getCompletionTime()).getSeconds();
        model.setRemainingSeconds(Math.max(0, seconds));

        return model;
    }
}