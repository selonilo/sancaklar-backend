package com.sc.sancaklar.model.dto;

import com.sc.sancaklar.model.enums.MovementType;
import lombok.Data;

@Data
public class SendTroopsRequest {
    private Long sourceVillageId;
    private Long targetVillageId;
    private MovementType type; // ATTACK veya SUPPORT

    private int spearmenAmount;
    private int swordsmanAmount;
    private int axemanAmount;
    private int archerAmount;
    private int scoutAmount;
    private int lightCavalryAmount;
    private int heavyCavalryAmount;
    private int ramAmount;
    private int catapultAmount;
    private int conquerorAmount;
}