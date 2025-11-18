package com.sc.sancaklar.model.mapper;

import com.sc.sancaklar.model.dto.PlayerModel;
import com.sc.sancaklar.model.entity.PlayerEntity;
import org.springframework.stereotype.Component;

@Component
public class PlayerConverter {

    public PlayerModel toModel(PlayerEntity entity) {
        if (entity == null) return null;

        PlayerModel model = new PlayerModel();
        model.setId(entity.getId());
        model.setPoints(entity.getPoints());

        // User Bilgisi
        if (entity.getUser() != null) {
            model.setUsername(entity.getUser().getUsername());
        }

        // Dünya Bilgisi
        if (entity.getWorld() != null) {
            model.setWorldName(entity.getWorld().getName());
        }

        // Klan Bilgisi (Varsa doldur, yoksa null kalsın)
        if (entity.getAlliance() != null) {
            model.setAllianceId(entity.getAlliance().getId());
            model.setAllianceName(entity.getAlliance().getName());
            model.setAllianceTag(entity.getAlliance().getTag());
            model.setAllianceRole(entity.getAllianceRole());
        }

        // Not: Rank (Sıralama) bilgisi genelde Entity'de tutulmaz,
        // anlık hesaplanır veya cache'den gelir. Şimdilik boş bırakıyoruz.

        return model;
    }

    // Model -> Entity dönüşümü genelde Player için gerekmez,
    // çünkü Player oluşturma işlemi Auth sırasında otomatik olur.
}