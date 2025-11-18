package com.sc.sancaklar.model.dto;

import com.sc.sancaklar.model.enums.UnitType;
import lombok.Data;

@Data
public class RecruitRequest {
    private Long villageId;
    private UnitType unitType;
    private int amount; // Kaç tane üretilecek?
}