package com.sc.sancaklar.model.dto;

import com.sc.sancaklar.model.enums.BuildingType;
import lombok.Data;

@Data
public class UpgradeBuildingRequest {
    private Long villageId;
    private BuildingType buildingType;
}