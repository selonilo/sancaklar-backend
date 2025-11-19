package com.sc.sancaklar.service;

import com.sc.sancaklar.model.dto.MovementTrackerModel;
import com.sc.sancaklar.model.dto.SendTroopsRequest;

import java.util.List;

public interface MovementService {
    void sendTroops(SendTroopsRequest request);
    List<MovementTrackerModel> getOutgoingMovementsByPlayer(Long playerId);
}
