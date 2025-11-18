package com.sc.sancaklar.model.mapper;

import com.sc.sancaklar.model.dto.UnitRecruitmentModel;
import com.sc.sancaklar.model.entity.UnitRecruitmentEntity;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class RecruitmentConverter {

    public UnitRecruitmentModel toModel(UnitRecruitmentEntity entity) {
        if (entity == null) return null;

        UnitRecruitmentModel model = new UnitRecruitmentModel();
        model.setId(entity.getId());
        model.setUnitType(entity.getUnitType());
        model.setQuantity(entity.getQuantity());
        model.setCompletionTime(entity.getCompletionTime());

        // Kalan süre hesabı
        long seconds = Duration.between(LocalDateTime.now(), entity.getCompletionTime()).getSeconds();
        model.setRemainingSeconds(Math.max(0, seconds));

        return model;
    }
}