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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "village_buildings")
public class VillageBuildingsEntity extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "village_id")
    private VillageEntity village;

    // Binaların Seviyeleri
    private int headquarters = 1; // Ana Bina (Başlangıç 1)
    private int barracks = 0;     // Kışla
    private int stable = 0;       // Ahır
    private int workshop = 0;     // Atölye
    private int academy = 0;      // Akademi
    private int smithy = 0;       // Demirci
    private int market = 0;       // Pazar

    // Kaynak Binaları
    private int timberCamp = 0;       // Oduncu
    private int meatProduction = 0;   // Et Üretim (Kil yerine)
    private int ironMine = 0;         // Demir Madeni

    private int farm = 1;      // Çiftlik
    private int warehouse = 1; // Depo
    private int wall = 0;      // Sur
}
