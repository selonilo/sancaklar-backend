package com.sc.sancaklar.repository;

import com.sc.sancaklar.model.entity.VillageResearchesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VillageResearchesRepository extends JpaRepository<VillageResearchesEntity, Long> {
}
