package com.sc.sancaklar.service;

import com.sc.sancaklar.model.dto.JoinWorldRequest;
import com.sc.sancaklar.model.dto.WorldModel;
import com.sc.sancaklar.model.dto.village.VillageModel;

import java.util.List;

public interface WorldService {
    WorldModel createWorld(WorldModel worldModel);
    List<WorldModel> getActiveWorlds(Long userId);
    List<WorldModel> getAllWorlds();
    void deleteWorld(Long worldId);
    WorldModel deactivateWorld(Long worldId);
    List<VillageModel> enterWorld(JoinWorldRequest request);
}
