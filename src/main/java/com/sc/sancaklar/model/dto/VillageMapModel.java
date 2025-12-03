package com.sc.sancaklar.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VillageMapModel {
    private Long id;
    private String name;
    private int xcoord;
    private int ycoord;
    private int points;

    // Player Bilgileri
    private Long playerId;
    private String playerName;

    // İstersen Klan bilgisini de buraya ekleyebilirsin ileride
    private Long allianceId;
    private String allianceName;
}
