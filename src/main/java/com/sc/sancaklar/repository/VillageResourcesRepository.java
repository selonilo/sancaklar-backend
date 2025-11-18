package com.sc.sancaklar.repository;

import com.sc.sancaklar.model.entity.VillageResourcesEntity;
import com.sc.sancaklar.model.entity.WorldEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VillageResourcesRepository extends JpaRepository<VillageResourcesEntity, Long> {
}
