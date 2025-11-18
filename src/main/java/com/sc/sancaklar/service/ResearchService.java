package com.sc.sancaklar.service;

import com.sc.sancaklar.model.dto.ResearchQueueModel;
import com.sc.sancaklar.model.dto.VillageResearchesModel;
import com.sc.sancaklar.model.entity.VillageResearchesEntity;
import com.sc.sancaklar.model.enums.UnitType;

import java.util.List;

public interface ResearchService {
    void startResearch(Long villageId, UnitType unitType);
    VillageResearchesModel getResearches(Long villageId);
    List<ResearchQueueModel> getResearchQueue(Long villageId);
    boolean isResearched(VillageResearchesEntity r, UnitType type);
}
