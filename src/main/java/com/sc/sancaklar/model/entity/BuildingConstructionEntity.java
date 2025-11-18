package com.sc.sancaklar.model.entity;

import com.sc.sancaklar.model.entity.base.BaseEntity;
import com.sc.sancaklar.model.enums.BuildingType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "building_constructions", indexes = {
        @Index(name = "idx_completion_time", columnList = "completionTime")
})
public class BuildingConstructionEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "village_id", nullable = false)
    private VillageEntity village;

    @Enumerated(EnumType.STRING)
    private BuildingType buildingType;

    private int targetLevel; // İnşaat bitince bina bu seviyeye gelecek

    // İnşaatın başladığı zaman
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime completionTime;
}
