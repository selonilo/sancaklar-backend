package com.sc.sancaklar.repository;

import com.sc.sancaklar.model.entity.PlayerEntity;
import com.sc.sancaklar.model.entity.UserEntity;
import com.sc.sancaklar.model.entity.WorldEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {
    // Aynı kullanıcı aynı dünyada tekrar kayıt olamasın diye kontrol
    boolean existsByUserAndWorld(UserEntity user, WorldEntity world);

    // Kullanıcının o dünyadaki profilini bulmak için (Login sonrası)
    Optional<PlayerEntity> findByUserAndWorld(UserEntity user, WorldEntity world);
}
