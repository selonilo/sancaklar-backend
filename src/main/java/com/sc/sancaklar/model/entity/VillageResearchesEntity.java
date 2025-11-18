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
@Table(name = "village_researches")
public class VillageResearchesEntity extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "village_id", nullable = false)
    private VillageEntity village;

    // 0: Araştırılmadı (Kilitli)
    // 1: Araştırıldı (Üretilebilir)
    // Not: İleride 3 seviyeli sistem yaparsan 2 ve 3 de olabilir.

    private int spearmen = 0;      // Mızraklı
    private int swordsmen = 0;     // Kılıçlı
    private int axemen = 0;        // Baltalı
    private int archers = 0;       // Okçu
    private int scouts = 0;        // Casus
    private int lightCavalry = 0;  // Hafif Atlı
    private int heavyCavalry = 0;  // Ağır Atlı
    private int rams = 0;          // Şahmerdan
    private int catapults = 0;     // Mancınık
    private int conquerors = 0;    // Fetihçi
}
