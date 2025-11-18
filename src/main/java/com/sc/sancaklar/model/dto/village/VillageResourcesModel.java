package com.sc.sancaklar.model.dto.village;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VillageResourcesModel {
    private double woodAmount;
    private double meatAmount;
    private double ironAmount;
    private int storageCapacity; // Depo kapasitesini de dönmek iyi olur
    private LocalDateTime lastUpdated;
    private int woodHourlyProduction;
    private int meatHourlyProduction;
    private int ironHourlyProduction;
}