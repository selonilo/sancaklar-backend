package com.sc.sancaklar.model.dto.base;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseModel {
    private Long id;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String createdBy;
    private String updatedBy;
}
