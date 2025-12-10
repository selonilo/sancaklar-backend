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
        model.setSpearman(entity.getSpearman());
        model.setSwordsman(entity.getSwordsman());
        model.setAxeman(entity.getAxeman());
        model.setArcher(entity.getArcher());
        model.setScout(entity.getScout());
        model.setLightCavalry(entity.getLightCavalry());
        model.setHeavyCavalry(entity.getHeavyCavalry());
        model.setRam(entity.getRam());
        model.setCatapult(entity.getCatapult());
        model.setConqueror(entity.getConqueror());

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