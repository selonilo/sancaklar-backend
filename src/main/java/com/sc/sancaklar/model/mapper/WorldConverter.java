package com.sc.sancaklar.model.mapper;

import com.sc.sancaklar.model.dto.WorldModel;
import com.sc.sancaklar.model.entity.WorldEntity;
import org.springframework.stereotype.Component;

@Component
public class WorldConverter {
    // Entity -> Model (Veritabanından okurken)
    public WorldModel toModel(WorldEntity entity) {
        if (entity == null) return null;

        WorldModel model = new WorldModel();
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setSpeed(entity.getSpeed());
        model.setActive(entity.isActive());
        return model;
    }

    // Model -> Entity (Kaydederken/Güncellerken)
    public WorldEntity toEntity(WorldModel model) {
        if (model == null) return null;

        WorldEntity entity = new WorldEntity();
        // ID'yi set etmiyoruz, yeni kayıtta ID null olmalı, güncelemede repo halleder
        if (model.getId() != null) {
            entity.setId(model.getId());
        }
        entity.setName(model.getName());
        entity.setSpeed(model.getSpeed());
        entity.setActive(model.isActive());
        return entity;
    }
}
