package com.sc.sancaklar.model.dto;

import lombok.Data;

@Data
public class WorldModel {
    private Long id;
    private String name;
    private int speed;
    private boolean isActive;
    private boolean isJoining = false;
}
