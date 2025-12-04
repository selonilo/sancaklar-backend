package com.sc.sancaklar.service;

import com.sc.sancaklar.model.data.GameCalculator;
import com.sc.sancaklar.model.dto.*;
import com.sc.sancaklar.model.dto.village.VillageBuildingsModel;
import com.sc.sancaklar.model.dto.village.VillageModel;
import com.sc.sancaklar.model.entity.*;
import com.sc.sancaklar.model.enums.BuildingType;
import com.sc.sancaklar.model.enums.RegionDirection;
import com.sc.sancaklar.model.mapper.ConstructionConverter;
import com.sc.sancaklar.model.mapper.VillageConverter;
import com.sc.sancaklar.repository.*;
import lombok.RequiredArgsConstructor;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {

    private final UserRepository userRepository;
    private final VillageRepository villageRepository;
    private final WorldRepository worldRepository;

    @Override
    public ActiveCountModel getActiveCount() {
        ActiveCountModel countModel = new ActiveCountModel();
        countModel.setActiveUser(userRepository.count());
        countModel.setVillage(villageRepository.count());
        countModel.setWorld(worldRepository.count());
        return countModel;
    }
}