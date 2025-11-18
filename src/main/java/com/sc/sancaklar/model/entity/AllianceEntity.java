package com.sc.sancaklar.model.entity;

import com.sc.sancaklar.model.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "alliances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllianceEntity extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name; // Örn: "Sancaklar Birliği"

    @Column(nullable = false, unique = true, length = 6)
    private String tag; // Örn: "SNCK" (Kısa ve benzersiz olmalı)

    @Column(length = 5000) // Uzun bir klan profili yazısı
    private String description;

    @Column(length = 1000)
    private String internalAnnouncement; // Sadece üyelerin görebileceği duyuru panosu

    private int totalPoints; // Üyelerin puanlarının toplamı (Sıralama için)
    private int rank;        // Dünya sıralamasındaki yeri

    private LocalDateTime createdAt; // Kuruluş tarihi

    // Klana üye olan oyuncular
    @OneToMany(mappedBy = "alliance", fetch = FetchType.LAZY)
    private List<PlayerEntity> members = new ArrayList<>();
}
