package com.sc.sancaklar.model.entity;

import com.sc.sancaklar.model.entity.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "village_resources")
public class VillageResourcesEntity extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "village_id")
    private VillageEntity village;

    private double woodAmount;
    private double meatAmount;
    private double ironAmount;

    // Kaynakların en son hesaplandığı zaman
    private LocalDateTime lastUpdated;

    // Depo kapasitesi doldu mu kontrolü için helper metodlar gerekecek
}
