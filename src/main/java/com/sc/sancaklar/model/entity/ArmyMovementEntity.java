package com.sc.sancaklar.model.entity;

import com.sc.sancaklar.model.entity.base.BaseEntity;
import com.sc.sancaklar.model.enums.MovementType;
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
@Table(name = "army_movements", indexes = {
        @Index(name = "idx_arrival_time", columnList = "arrivalTime")
})
public class ArmyMovementEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "source_village_id", nullable = false)
    private VillageEntity sourceVillage; // Kim gönderdi?

    @ManyToOne
    @JoinColumn(name = "target_village_id", nullable = false)
    private VillageEntity targetVillage; // Nereye gidiyor?

    @Enumerated(EnumType.STRING)
    private MovementType type;

    private LocalDateTime startTime;
    private LocalDateTime arrivalTime;

    @Column(nullable = false)
    private boolean isProcessed = false;

    // --- TAŞINAN ASKERLER ---
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

    // --- TAŞINAN GANİMET (Geri dönüşte dolu olur) ---
    private double woodCarried;
    private double meatCarried;
    private double ironCarried;
}
