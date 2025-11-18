package com.sc.sancaklar.model.dto;

import com.sc.sancaklar.model.enums.UnitType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UnitRecruitmentModel {
    private Long id;
    private UnitType unitType;
    private int quantity;
    private LocalDateTime completionTime;
    private long remainingSeconds; // Geri sayım için
}