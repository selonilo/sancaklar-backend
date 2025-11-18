package com.sc.sancaklar.model.mapper;

import com.sc.sancaklar.model.dto.ConstructionModel;
import com.sc.sancaklar.model.entity.BuildingConstructionEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class ConstructionConverter {

    public ConstructionModel toModel(BuildingConstructionEntity entity) {
        if (entity == null) return null;

        ConstructionModel model = new ConstructionModel();
        model.setId(entity.getId());
        model.setBuildingType(entity.getBuildingType());
        model.setTargetLevel(entity.getTargetLevel());
        model.setStartTime(entity.getStartTime());
        model.setCompletionTime(entity.getCompletionTime());

        // Kalan süreyi hesapla (Eksiye düşmesin diye Math.max)
        long seconds = Duration.between(LocalDateTime.now(), entity.getCompletionTime()).getSeconds();
        model.setRemainingSeconds(Math.max(0, seconds));

        return model;
    }
}