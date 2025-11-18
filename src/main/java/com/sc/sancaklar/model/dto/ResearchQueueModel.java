package com.sc.sancaklar.model.dto;

import com.sc.sancaklar.model.enums.UnitType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResearchQueueModel {
    private Long id;
    private UnitType unitType; // Araştırılan birim
    private LocalDateTime startTime;
    private LocalDateTime completionTime;
    private long remainingSeconds; // Geri sayım için
}