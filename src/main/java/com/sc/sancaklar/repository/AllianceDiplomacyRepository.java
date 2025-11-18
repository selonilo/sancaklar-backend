package com.sc.sancaklar.repository;

import com.sc.sancaklar.model.entity.AllianceDiplomacyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllianceDiplomacyRepository extends JpaRepository<AllianceDiplomacyEntity, Long> {
    // Bir klanın tüm diplomatik ilişkilerini (dost/düşman) getir
    // Hem talep eden hem hedef olan taraf olabilir, bu yüzden JPA Query ile yazmak daha temizdir
    // Ama şimdilik basit tutalım, custom query:

    // Select * from diplomacy where requester_id = ? OR target_id = ?
    List<AllianceDiplomacyEntity> findByRequesterAllianceIdOrTargetAllianceId(Long allianceId1, Long allianceId2);
}
