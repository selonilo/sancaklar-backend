package com.sc.sancaklar.model.dto;

import com.sc.sancaklar.model.enums.BuildingType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConstructionModel {
    private Long id;
    private BuildingType buildingType;
    private int targetLevel; // Hedeflenen seviye
    private LocalDateTime startTime;
    private LocalDateTime completionTime;

    private long remainingSeconds; // Frontend'e kolaylık olsun diye kalan süreyi de hesaplayıp yollayalım
}