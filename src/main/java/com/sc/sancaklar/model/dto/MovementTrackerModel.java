package com.sc.sancaklar.model.dto;

import com.sc.sancaklar.model.enums.MovementType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MovementTrackerModel {
    private Long movementId;
    private Long sourceVillageId;
    private Long targetVillageId;
    private String targetVillageName;
    private MovementType type;
    private LocalDateTime arrivalTime;

    // Yoldaki asker sayılarının özeti de eklenebilir.
}