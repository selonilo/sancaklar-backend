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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "research_queue", indexes = {
        @Index(name = "idx_research_completion", columnList = "completionTime")
})
public class ResearchQueueEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "village_id", nullable = false)
    private VillageEntity village;

    @Enumerated(EnumType.STRING)
    private UnitType unitType; // Hangi asker araştırılıyor?

    private LocalDateTime startTime;
    private LocalDateTime completionTime;
}
