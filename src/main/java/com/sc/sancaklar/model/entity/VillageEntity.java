package com.sc.sancaklar.model.entity;

import com.sc.sancaklar.model.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "villages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VillageEntity extends BaseEntity {
    private String name;
    private int xcoord;
    private int ycoord;
    private int points;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private PlayerEntity player;

    // --- BİNALAR ---
    @OneToOne(mappedBy = "village", cascade = CascadeType.ALL)
    private VillageBuildingsEntity buildings;

    // --- KAYNAKLAR ---
    @OneToOne(mappedBy = "village", cascade = CascadeType.ALL)
    private VillageResourcesEntity resources;

    // --- ASKERLER ---
    @OneToOne(mappedBy = "village", cascade = CascadeType.ALL)
    private VillageTroopsEntity troops;

    // --- ARAŞTIRMA ---
    @OneToOne(mappedBy = "village", cascade = CascadeType.ALL)
    private VillageResearchesEntity researches;

    @ManyToOne
    @JoinColumn(name = "world_id", nullable = false)
    private WorldEntity world;

    private int loyalty = 100;
}
