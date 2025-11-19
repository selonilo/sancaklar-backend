package com.sc.sancaklar.model.entity;

import com.sc.sancaklar.model.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stationed_troops")
public class StationedTroopsEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "location_village_id", nullable = false)
    private VillageEntity locationVillage; // Askerler şu an nerede?

    @ManyToOne
    @JoinColumn(name = "owner_village_id", nullable = false)
    private VillageEntity ownerVillage; // Askerlerin asıl sahibi kim?

    // Asker Sayıları
    private int spearmen;
    private int swordsmen;
    private int axemen;
    private int archers;
    private int scouts;
    private int lightCavalry;
    private int heavyCavalry;
    private int rams;
    private int catapults;
    private int conquerors;
}
