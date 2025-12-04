package com.sc.sancaklar.model.entity;

import com.sc.sancaklar.model.entity.base.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "WORLDS")
public class WorldEntity extends BaseEntity {
    private String name;
    private int speed; // Oyun hızı (inşaat ve üretim için çarpan)
    private boolean isActive;

    // Dünya silinince içindeki tüm OYUNCULARI da sil
    @OneToMany(mappedBy = "world", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerEntity> players;

    // Dünya silinince içindeki tüm KÖYLERİ (Barbarlar dahil) sil
    @OneToMany(mappedBy = "world", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VillageEntity> villages;
}
