package com.sc.sancaklar.repository;

import com.sc.sancaklar.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String mail);
    Optional<UserEntity> findByUsername(String username);
    List<UserEntity> findByIdIn(List<Long> id);
}
