package com.sc.sancaklar.model.entity.base;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

public class BaseEntityListener {
    @PrePersist
    public void prePersist(BaseEntity entity) {
        entity.setCreatedDate(LocalDateTime.now());
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setCreatedBy(getCurrentUser());
        entity.setUpdatedBy(getCurrentUser());
    }

    @PreUpdate
    public void preUpdate(BaseEntity entity) {
        entity.setUpdatedDate(LocalDateTime.now());
        entity.setUpdatedBy(getCurrentUser());
    }

    private String getCurrentUser() {
        String name;
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            name = SecurityContextHolder.getContext().getAuthentication().getName();
        } else {
            name = "SYSTEM";
        }
        return name;
    }
}
