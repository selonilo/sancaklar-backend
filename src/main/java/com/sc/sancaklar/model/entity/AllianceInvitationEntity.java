package com.sc.sancaklar.model.entity;

import com.sc.sancaklar.model.entity.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "alliance_invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllianceInvitationEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "alliance_id", nullable = false)
    private AllianceEntity alliance; // Davet eden klan

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerEntity player; // Davet edilen oyuncu

    // Daveti gönderen yetkili oyuncu (Kimin davet ettiğini bilmek iyidir)
    @ManyToOne
    @JoinColumn(name = "inviter_id")
    private PlayerEntity inviter;

    private LocalDateTime sentAt; // Davet zamanı
}
