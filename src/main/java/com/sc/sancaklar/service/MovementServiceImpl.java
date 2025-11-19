package com.sc.sancaklar.service;

import com.sc.sancaklar.model.data.GameCalculator;
import com.sc.sancaklar.model.dto.MovementTrackerModel;
import com.sc.sancaklar.model.dto.SendTroopsRequest;
import com.sc.sancaklar.model.entity.*;
import com.sc.sancaklar.model.enums.UnitType;
import com.sc.sancaklar.repository.ArmyMovementRepository;
import com.sc.sancaklar.repository.VillageRepository;
import com.sc.sancaklar.repository.VillageTroopsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MovementServiceImpl implements MovementService {
    private final VillageRepository villageRepository;
    private final VillageTroopsRepository troopsRepository;
    private final ArmyMovementRepository movementRepository;

    @Transactional
    public void sendTroops(SendTroopsRequest request) {
        // 1. Köyleri Bul
        VillageEntity source = villageRepository.findById(request.getSourceVillageId())
                .orElseThrow(() -> new RuntimeException("Kaynak köy bulunamadı!"));
        VillageEntity target = villageRepository.findById(request.getTargetVillageId())
                .orElseThrow(() -> new RuntimeException("Hedef köy bulunamadı!"));

        if (source.getId().equals(target.getId())) {
            throw new RuntimeException("Askerler aynı köye gönderilemez!");
        }

        // 2. Asker Kontrolü ve Düşümü (Transaction içinde yapılmalı)
        VillageTroopsEntity troops = source.getTroops();
        checkAndSubtractTroops(troops, request);
        troopsRepository.save(troops);

        // 3. Mesafeyi ve Hızı Hesapla
        double distance = calculateDistance(source.getXcoord(), source.getYcoord(), target.getXcoord(), target.getYcoord());

        // En yavaş birimin hızını (dakika/alan cinsinden) bul
        int slowestSpeedInMinutesPerField = findSlowestUnitSpeed(request);

        // Dünya Hızını dahil et (Varsayılan 1, WorldEntity'den çekilmeli)
        int worldSpeed = source.getPlayer().getWorld().getSpeed();

        // Süre = Mesafe * En Yavaş Birim Süresi (dakika/alan) * 60 saniye / Dünya Hızı
        long durationSeconds = (long) (distance * slowestSpeedInMinutesPerField * 60) / worldSpeed;

        // Minimum süre kısıtlaması (Örn: 10 saniye)
        durationSeconds = Math.max(durationSeconds, 10);

        // 4. Hareketi Oluştur ve Kaydet
        ArmyMovementEntity movement = new ArmyMovementEntity();
        movement.setSourceVillage(source);
        movement.setTargetVillage(target);
        movement.setType(request.getType());
        movement.setStartTime(LocalDateTime.now());
        movement.setArrivalTime(LocalDateTime.now().plusSeconds(durationSeconds));

        // Askerleri Movement Entity'ye set et
        setMovementUnits(movement, request);

        movementRepository.save(movement);
    }

    // --- HELPER METODLAR ---

    /**
     * İstenen asker sayılarının köyde olup olmadığını kontrol eder ve varsa köy stoğundan düşer.
     * @param troops Köyün mevcut asker stoğu.
     * @param request Gönderilmek istenen miktar.
     */
    private void checkAndSubtractTroops(VillageTroopsEntity troops, SendTroopsRequest request) {
        if (troops.getSpearmen() < request.getSpearmenAmount()) throw new RuntimeException("Yetersiz Mızraklı!");
        if (troops.getSwordsmen() < request.getSwordsmanAmount()) throw new RuntimeException("Yetersiz Kılıçlı!");
        // ... (Diğer tüm birimler için kontrol ve hata fırlatma) ...

        // Düşme işlemi
        troops.setSpearmen(troops.getSpearmen() - request.getSpearmenAmount());
        troops.setSwordsmen(troops.getSwordsmen() - request.getSwordsmanAmount());
        troops.setAxemen(troops.getAxemen() - request.getAxemanAmount());
        troops.setArchers(troops.getArchers() - request.getArcherAmount());
        troops.setScouts(troops.getScouts() - request.getScoutAmount());
        troops.setLightCavalry(troops.getLightCavalry() - request.getLightCavalryAmount());
        troops.setHeavyCavalry(troops.getHeavyCavalry() - request.getHeavyCavalryAmount());
        troops.setRams(troops.getRams() - request.getRamAmount());
        troops.setCatapults(troops.getCatapults() - request.getCatapultAmount());
        troops.setConquerors(troops.getConquerors() - request.getConquerorAmount());
    }

    /**
     * Hareket eden entity'ye asker sayılarını set eder.
     */
    private void setMovementUnits(ArmyMovementEntity movement, SendTroopsRequest request) {
        movement.setSpearmen(request.getSpearmenAmount());
        movement.setSwordsmen(request.getSwordsmanAmount());
        movement.setAxemen(request.getAxemanAmount());
        movement.setArchers(request.getArcherAmount());
        movement.setScouts(request.getScoutAmount());
        movement.setLightCavalry(request.getLightCavalryAmount());
        movement.setHeavyCavalry(request.getHeavyCavalryAmount());
        movement.setRams(request.getRamAmount());
        movement.setCatapults(request.getCatapultAmount());
        movement.setConquerors(request.getConquerorAmount());
    }

    /**
     * Pisagor teoremi ile iki koordinat arası mesafeyi (alan/field cinsinden) hesaplar.
     */
    private double calculateDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    /**
     * Gönderilen birlik içindeki en yavaş birimin hızını (dakika/alan) bulur.
     */
    private int findSlowestUnitSpeed(SendTroopsRequest request) {

        // İstekteki her bir birim tipini ve miktarını Stream ile birleştiririz.
        Stream<UnitType> requestedUnits = Stream.of(
                getUnitStream(request.getSpearmenAmount(), UnitType.SPEARMAN),
                getUnitStream(request.getSwordsmanAmount(), UnitType.SWORDSMAN),
                getUnitStream(request.getAxemanAmount(), UnitType.AXEMAN),
                getUnitStream(request.getArcherAmount(), UnitType.ARCHER),
                getUnitStream(request.getScoutAmount(), UnitType.SCOUT),
                getUnitStream(request.getLightCavalryAmount(), UnitType.LIGHT_CAVALRY),
                getUnitStream(request.getHeavyCavalryAmount(), UnitType.HEAVY_CAVALRY),
                getUnitStream(request.getRamAmount(), UnitType.RAM),
                getUnitStream(request.getCatapultAmount(), UnitType.CATAPULT),
                getUnitStream(request.getConquerorAmount(), UnitType.CONQUEROR)
        ).flatMap(s -> s); // Stream'leri tek bir Stream'de birleştir

        // En yavaş birim (En yüksek dakika/alan değerine sahip olan)
        return requestedUnits
                .map(GameCalculator::getSpeedInMinutes) // Hız değerine çevir
                .max(Comparator.naturalOrder())         // En yükseği (en yavaşı) bul
                .orElse(GameCalculator.getSpeedInMinutes(UnitType.SPEARMAN)); // Eğer hiç asker göndermezse default mızraklı hızı

    }

    // Yardımcı Stream metodu
    private Stream<UnitType> getUnitStream(int amount, UnitType type) {
        return amount > 0 ? Stream.of(type) : Stream.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovementTrackerModel> getOutgoingMovementsByPlayer(Long playerId) {

        List<ArmyMovementEntity> activeMovements =
                movementRepository.findBySourceVillage_Player_IdAndIsProcessedFalse(playerId);

        return activeMovements.stream()
                .map(this::toTrackerModel)
                .toList();
    }

    private MovementTrackerModel toTrackerModel(ArmyMovementEntity entity) {
        MovementTrackerModel model = new MovementTrackerModel();
        model.setMovementId(entity.getId());
        model.setSourceVillageId(entity.getSourceVillage().getId());
        model.setTargetVillageId(entity.getTargetVillage().getId());
        model.setTargetVillageName(entity.getTargetVillage().getName()); // Hedef köy adını da gösterelim
        model.setType(entity.getType());
        model.setArrivalTime(entity.getArrivalTime());

        return model;
    }
}
