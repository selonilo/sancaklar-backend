package com.sc.sancaklar.model.dto;

import com.sc.sancaklar.model.enums.RegionDirection;
import lombok.Data;

@Data
public class JoinWorldRequest {
    private Long userId;
    private Long worldId;
    private RegionDirection direction;
}