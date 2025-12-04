package com.sc.sancaklar.service;

import com.sc.sancaklar.model.dto.ConstructionModel;
import com.sc.sancaklar.model.dto.PlayerModel;
import com.sc.sancaklar.model.dto.UpgradeBuildingRequest;
import com.sc.sancaklar.model.dto.VillageMapModel;
import com.sc.sancaklar.model.dto.village.VillageModel;
import com.sc.sancaklar.model.entity.VillageEntity;
import com.sc.sancaklar.model.enums.RegionDirection;

import java.util.List;

public interface VillageService {
    VillageModel getVillageById(Long villageId);
    VillageModel createFirstVillage(PlayerModel playerModel, RegionDirection direction);
    List<VillageModel> getVillagesByPlayerId(Long playerId);
    ConstructionModel upgradeBuilding(UpgradeBuildingRequest request);
    List<ConstructionModel> getConstructionQueue(Long villageId);
    List<VillageMapModel> getListByWorldId(Long worldId);
    void cancelConstruction(Long constructionId);
    void createInitialBuildings(VillageEntity village);
    void createInitialResources(VillageEntity village);
    void update(VillageModel villageModel);
}
