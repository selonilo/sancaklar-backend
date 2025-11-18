package com.sc.sancaklar.repository;

import com.sc.sancaklar.model.entity.AllianceInvitationEntity;
import com.sc.sancaklar.model.entity.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllianceInvitationRepository extends JpaRepository<AllianceInvitationEntity, Long> {
    List<AllianceInvitationEntity> findByPlayer(PlayerEntity player); // Bana gelen davetler
}
