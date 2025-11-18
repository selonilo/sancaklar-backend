package com.sc.sancaklar.service;

import com.sc.sancaklar.model.dto.RecruitRequest;
import com.sc.sancaklar.model.dto.UnitRecruitmentModel;
import com.sc.sancaklar.model.enums.BuildingType;

import java.util.List;

public interface RecruitmentService {
    void recruitUnits(RecruitRequest request);
    List<UnitRecruitmentModel> getRecruitmentQueue(Long villageId, BuildingType buildingType);
}
