package com.sc.sancaklar.model.entity;

import com.sc.sancaklar.model.entity.base.BaseEntity;
import com.sc.sancaklar.model.enums.DiplomacyType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "alliance_diplomacy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllianceDiplomacyEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "requester_alliance_id", nullable = false)
    private AllianceEntity requesterAlliance; // İlişkiyi başlatan klan

    @ManyToOne
    @JoinColumn(name = "target_alliance_id", nullable = false)
    private AllianceEntity targetAlliance; // Hedef klan

    @Enumerated(EnumType.STRING)
    private DiplomacyType type; // ALLY (Müttefik), NAP (Saldırmazlık), ENEMY (Düşman)

    private boolean isAccepted; // Düşmanlık tek taraflı olabilir ama Müttefiklik onay gerektirir
}
