package com.sc.sancaklar.model.dto;

import com.sc.sancaklar.model.enums.UnitType;
import lombok.Data;

@Data
public class ResearchRequest {
    private Long villageId;
    private UnitType unitType; // Örn: "SWORDSMAN"
}