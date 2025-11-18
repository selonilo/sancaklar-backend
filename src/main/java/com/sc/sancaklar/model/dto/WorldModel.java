package com.sc.sancaklar.model.dto;

import lombok.Data;

@Data
public class WorldModel {
    private Long id;          // Güncelleme veya okuma yaparken lazım
    private String name;      // Dünya adı
    private int speed;        // Oyun hızı
    private boolean isActive; // Aktiflik durumu
}
