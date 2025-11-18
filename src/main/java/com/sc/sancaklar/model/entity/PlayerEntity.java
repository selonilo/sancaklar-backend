package com.sc.sancaklar.model.entity;

import com.sc.sancaklar.model.entity.base.BaseEntity;
import com.sc.sancaklar.model.enums.AllianceRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "players", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "world_id"}) // Bir user aynı dünyada iki kere olamaz
})
public class PlayerEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "world_id", nullable = false)
    private WorldEntity world;

    private int points; // Köylerin toplam puanı

    @ManyToOne(fetch = FetchType.LAZY) // Oyuncuyu çekerken klan detaylarını hemen çekmesin, performans için
    @JoinColumn(name = "alliance_id")
    private AllianceEntity alliance;

    @Enumerated(EnumType.STRING)
    private AllianceRole allianceRole; // Oyuncunun klandaki rütbesi

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<VillageEntity> villages;
}
