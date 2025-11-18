package com.sc.sancaklar.model.entity;

import com.sc.sancaklar.model.entity.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "WORLDS")
public class WorldEntity extends BaseEntity {
    private String name;
    private int speed; // Oyun hızı (inşaat ve üretim için çarpan)
    private boolean isActive;
}
