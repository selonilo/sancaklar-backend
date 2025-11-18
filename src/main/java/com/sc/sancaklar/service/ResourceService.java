package com.sc.sancaklar.service;

import com.sc.sancaklar.model.entity.VillageEntity;

public interface ResourceService {
    void calculateAndUpdateResources(VillageEntity village);
}
