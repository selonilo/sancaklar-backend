package com.sc.sancaklar.repository;

import com.sc.sancaklar.model.entity.VillageTroopsEntity;
import com.sc.sancaklar.model.entity.WorldEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VillageTroopsRepository extends JpaRepository<VillageTroopsEntity, Long> {
}
