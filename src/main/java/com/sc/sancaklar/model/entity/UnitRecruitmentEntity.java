package com.sc.sancaklar.model.entity;

import com.sc.sancaklar.model.entity.base.BaseEntity;
import com.sc.sancaklar.model.enums.UnitType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "unit_recruitments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnitRecruitmentEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "village_id", nullable = false)
    private VillageEntity village;

    @Enumerated(EnumType.STRING)
    private UnitType unitType;

    private int quantity; // Eğitilecek toplam asker sayısı (Örn: 50 Mızraklı)

    private int producedAmount = 0; // Şu ana kadar bu emirden kaçı üretildi? (Opsiyonel, detaylı takip için)

    // Bu emrin tamamen biteceği zaman
    private LocalDateTime completionTime;

    // Bir askerin üretim süresi (Saniye cinsinden).
    // Neden tutuyoruz? Çünkü sunucu durursa veya işlem yarım kalırsa kalan süreyi hesaplamak için.
    private int secondsPerUnit;
}
