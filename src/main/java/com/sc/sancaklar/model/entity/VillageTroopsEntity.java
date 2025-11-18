package com.sc.sancaklar.model.entity;

import com.sc.sancaklar.model.entity.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "village_troops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VillageTroopsEntity extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "village_id", nullable = false)
    private VillageEntity village;

    // Piyadeler (Kışla)
    private int spearmen = 0;
    private int swordsmen = 0;
    private int axemen = 0;
    private int archers = 0;

    // Atlılar (Ahır)
    private int scouts = 0;
    private int lightCavalry = 0;
    private int heavyCavalry = 0;

    // Kuşatma Silahları (Atölye)
    private int rams = 0;
    private int catapults = 0;

    // Özel (Saray/Akademi)
    private int conquerors = 0; // Fetihçi
}
