package com.sc.sancaklar.model.dto.village;

import lombok.Data;

@Data
public class VillageModel {
    private Long id;
    private String name;
    private int xCoord;
    private int yCoord;
    private int points;
    private int loyalty;

    private Long playerId;     // Sahibinin ID'si
    private String playerName; // Sahibinin Adı (Frontend'de göstermek için)

    // Alt Modeller (İlişkili veriler)
    private VillageBuildingsModel buildings;
    private VillageResourcesModel resources;
    private VillageTroopsModel troops;
}