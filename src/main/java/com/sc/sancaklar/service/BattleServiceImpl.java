package com.sc.sancaklar.service;

import com.sc.sancaklar.model.data.GameCalculator;
import com.sc.sancaklar.model.data.UnitAccess;
import com.sc.sancaklar.model.entity.*;
import com.sc.sancaklar.model.enums.MovementType;
import com.sc.sancaklar.model.enums.UnitType;
import com.sc.sancaklar.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class BattleServiceImpl implements BattleService {
    // REPOSITORY ENJEKSİYONLARI (Kullanılan tüm entity'lerin reposu)
    private final VillageRepository villageRepository;
    private final VillageTroopsRepository troopsRepository;
    private final VillageResourcesRepository resourcesRepository;
    private final VillageBuildingsRepository buildingsRepository;
    private final ArmyMovementRepository movementRepository;
    private final StationedTroopsRepository stationedTroopsRepository;
    private final MovementService movementService;

    private final Random random = new Random();

    // --- ANA SAVAŞ ÇÖZÜMLEME METODU ---
    // (Bu metodun iskeleti önceki cevapta verildi)

    @Override
    @Transactional
    public void resolveBattle(ArmyMovementEntity attackMovement) {

        VillageEntity defenderVillage = attackMovement.getTargetVillage();
        VillageTroopsEntity defenderTroops = defenderVillage.getTroops();
        VillageBuildingsEntity buildings = defenderVillage.getBuildings();
        List<StationedTroopsEntity> supportArmies = stationedTroopsRepository.findByLocationVillage(defenderVillage);

        // 1. GÜÇ HESAPLAMA
        double attackPower = calculateTotalPower(attackMovement, false);
        double totalDefensePower = calculateTotalPower(defenderTroops, true);

        double supportPower = supportArmies.stream()
                .mapToDouble(support -> calculateTotalPower(support, true))
                .sum();

        totalDefensePower += supportPower;

        // 2. SUR HASARI VE BONUS
        int initialWallLevel = buildings.getWall();
        int effectiveWallLevel = applyRamDamage(attackMovement, initialWallLevel);

        double wallBonus = 1 + (effectiveWallLevel * 0.04);
        double finalDefensePower = totalDefensePower * wallBonus;

        // 3. KAZANAN VE KAYIP HESABI
        double attackLossRatio;
        double defenseLossRatio;

        if (attackPower > finalDefensePower) {
            // SALDIRAN KAZANDI 🏆
            attackLossRatio = Math.min(1.0, Math.pow(finalDefensePower / attackPower, 1.5));
            defenseLossRatio = 1.0;
        } else {
            // SAVUNAN KAZANDI 🛡️
            defenseLossRatio = Math.min(1.0, Math.pow(attackPower / finalDefensePower, 1.5));
            attackLossRatio = 1.0;
        }

        // 4. KAYIPLARI UYGULA (TAMAMLANAN KISIM)
        applyCasualtiesToMovement(attackMovement, attackLossRatio);
        applyCasualtiesToTroops(defenderTroops, defenseLossRatio);
        applyCasualtiesToSupport(supportArmies, defenseLossRatio);

        // 5. BİNA YIKIMI
        if (attackPower > finalDefensePower && attackMovement.getCatapults() > 0) {
            applyCatapultDamage(attackMovement, buildings);
        }

        // 6. GANİMET VE GERİ DÖNÜŞ
        if (attackPower > finalDefensePower) {
            lootResources(attackMovement, defenderVillage);
        }

        if (hasSurvivors(attackMovement)) {
            createReturnMovement(attackMovement);
        }
    }

    // --- GÜÇ HESAPLAMA METODU ---

    /**
     * Verilen birlik kümesinin toplam saldırı veya savunma gücünü hesaplar.
     */
    private double calculateTotalPower(Object units, boolean isDefense) {

        // Units nesnesinin hangi Entity tipinde olduğunu kontrol et
        UnitAccess access = new UnitAccess(units);
        double totalPower = 0;

        // Tüm birim tipleri için döngü (Burada bir Enum Listesi ile dönmek daha temiz olurdu)
        totalPower += access.getSpearmen() * (isDefense ? GameCalculator.getDefensePower(UnitType.SPEARMAN) : GameCalculator.getAttackPower(UnitType.SPEARMAN));
        totalPower += access.getSwordsmen() * (isDefense ? GameCalculator.getDefensePower(UnitType.SWORDSMAN) : GameCalculator.getAttackPower(UnitType.SWORDSMAN));
        totalPower += access.getAxemen() * (isDefense ? GameCalculator.getDefensePower(UnitType.AXEMAN) : GameCalculator.getAttackPower(UnitType.AXEMAN));
        totalPower += access.getArchers() * (isDefense ? GameCalculator.getDefensePower(UnitType.ARCHER) : GameCalculator.getAttackPower(UnitType.ARCHER));
        totalPower += access.getScouts() * (isDefense ? GameCalculator.getDefensePower(UnitType.SCOUT) : GameCalculator.getAttackPower(UnitType.SCOUT));
        totalPower += access.getLightCavalry() * (isDefense ? GameCalculator.getDefensePower(UnitType.LIGHT_CAVALRY) : GameCalculator.getAttackPower(UnitType.LIGHT_CAVALRY));
        totalPower += access.getHeavyCavalry() * (isDefense ? GameCalculator.getDefensePower(UnitType.HEAVY_CAVALRY) : GameCalculator.getAttackPower(UnitType.HEAVY_CAVALRY));
        totalPower += access.getRams() * (isDefense ? GameCalculator.getDefensePower(UnitType.RAM) : GameCalculator.getAttackPower(UnitType.RAM));
        totalPower += access.getCatapults() * (isDefense ? GameCalculator.getDefensePower(UnitType.CATAPULT) : GameCalculator.getAttackPower(UnitType.CATAPULT));
        totalPower += access.getConquerors() * (isDefense ? GameCalculator.getDefensePower(UnitType.CONQUEROR) : GameCalculator.getAttackPower(UnitType.CONQUEROR));

        return totalPower;
    }


    // --- KAYIP UYGULAMA METODLARI ---

    /**
     * Savunucunun KENDİ askerlerine kaybı uygular. (troopsRepository.save çağırır)
     *
     * @param defenderTroops Savunucunun ana stoğu.
     * @param lossRatio      Kayıp oranı (0.0 ile 1.0 arası).
     */
    private void applyCasualtiesToTroops(VillageTroopsEntity defenderTroops, double lossRatio) {
        double factor = 1.0 - lossRatio; // Hayatta kalma oranı

        // Yeni Miktar = Eski Miktar * Hayatta Kalma Oranı
        defenderTroops.setSpearman((int) (defenderTroops.getSpearman() * factor));
        defenderTroops.setSwordsman((int) (defenderTroops.getSwordsman() * factor));
        defenderTroops.setAxeman((int) (defenderTroops.getAxeman() * factor));
        defenderTroops.setArcher((int) (defenderTroops.getArcher() * factor));
        defenderTroops.setScout((int) (defenderTroops.getScout() * factor));
        defenderTroops.setLightCavalry((int) (defenderTroops.getLightCavalry() * factor));
        defenderTroops.setHeavyCavalry((int) (defenderTroops.getHeavyCavalry() * factor));
        defenderTroops.setRam((int) (defenderTroops.getRam() * factor));
        defenderTroops.setCatapult((int) (defenderTroops.getCatapult() * factor));
        defenderTroops.setConqueror((int) (defenderTroops.getConqueror() * factor));

        troopsRepository.save(defenderTroops);
    }

    /**
     * Destek askerlerine kaybı uygular. Tamamen yok olan destek ordularını siler.
     *
     * @param supportArmies Destek orduları listesi.
     * @param lossRatio     Kayıp oranı (0.0 ile 1.0 arası).
     */
    private void applyCasualtiesToSupport(List<StationedTroopsEntity> supportArmies, double lossRatio) {
        double factor = 1.0 - lossRatio;

        for (StationedTroopsEntity support : supportArmies) {
            // Kayıp uygula
            support.setSpearmen((int) (support.getSpearmen() * factor));
            support.setSwordsmen((int) (support.getSwordsmen() * factor));
            support.setAxemen((int) (support.getAxemen() * factor));
            support.setArchers((int) (support.getArchers() * factor));
            support.setScouts((int) (support.getScouts() * factor));
            support.setLightCavalry((int) (support.getLightCavalry() * factor));
            support.setHeavyCavalry((int) (support.getHeavyCavalry() * factor));
            support.setRams((int) (support.getRams() * factor));
            support.setCatapults((int) (support.getCatapults() * factor));
            support.setConquerors((int) (support.getConquerors() * factor));

            // Tamamen yok olan birliği silme kontrolü
            if (support.getSpearmen() + support.getSwordsmen() + support.getAxemen() + support.getArchers() +
                    support.getScouts() + support.getLightCavalry() + support.getHeavyCavalry() +
                    support.getRams() + support.getCatapults() + support.getConquerors() == 0) {
                stationedTroopsRepository.delete(support);
            } else {
                stationedTroopsRepository.save(support);
            }
        }
    }

    /**
     * Catapult hasarını uygular (Ana Bina yıkımı).
     *
     * @param attackMovement Saldırganın hareket kaydı.
     * @param buildings      Savunucunun bina seviyeleri.
     */
    private void applyCatapultDamage(ArmyMovementEntity attackMovement, VillageBuildingsEntity buildings) {

        int survivingCatapults = attackMovement.getCatapults();
        if (survivingCatapults == 0) return;

        // Hasar seviyesi hesaplama (Örn: Her 50 Catapult 1 seviye düşürür + minimum 1 seviye hasar)
        int levelReduction = Math.max(1, survivingCatapults / 50);

        // Hedef Bina: Ana Bina (Headquarters)
        int currentLevel = buildings.getHeadquarters();
        int newLevel = Math.max(0, currentLevel - levelReduction);

        buildings.setHeadquarters(newLevel);

        // Eğer köyün yıkılması isteniyorsa (HQ=0), burada ek mantık yazılmalıdır.
        buildingsRepository.save(buildings);
    }

    // Ram Hasarını Uygula (Duvar seviyesini düşürür - resolveBattle içinde çağrılır)
    private int applyRamDamage(ArmyMovementEntity movement, int wallLevel) {
        int ramDamage = movement.getRams();
        int reduction = (int) (ramDamage * 0.1); // Her 10 ram 1 seviye düşürsün
        int newWallLevel = Math.max(0, wallLevel - reduction);

        movement.getTargetVillage().getBuildings().setWall(newWallLevel);
        buildingsRepository.save(movement.getTargetVillage().getBuildings());
        return newWallLevel;
    }

    // --- DİĞER HELPERLAR (Eksiksiz hale getirilmelidir) ---

    private void applyCasualtiesToMovement(ArmyMovementEntity movement, double lossRatio) {
        double factor = 1.0 - lossRatio;

        movement.setSpearmen((int) (movement.getSpearmen() * factor));
        movement.setSwordsmen((int) (movement.getSwordsmen() * factor));
        movement.setAxemen((int) (movement.getAxemen() * factor));
        movement.setArchers((int) (movement.getArchers() * factor));
        movement.setScouts((int) (movement.getScouts() * factor));
        movement.setLightCavalry((int) (movement.getLightCavalry() * factor));
        movement.setHeavyCavalry((int) (movement.getHeavyCavalry() * factor));
        movement.setRams((int) (movement.getRams() * factor));
        movement.setCatapults((int) (movement.getCatapults() * factor));
        movement.setConquerors((int) (movement.getConquerors() * factor));
    }

    private void lootResources(ArmyMovementEntity movement, VillageEntity defender) {

        // Saldırganın kaynakları zaten GameLoop'ta günceldir varsayıyoruz.
        VillageResourcesEntity defenderResources = defender.getResources();

        // --- 1. Toplam Taşıma Kapasitesini Hesapla ---
        // Hayatta kalan her birimin taşıma gücünü toplar.
        double totalCarryCapacity = calculateTotalCarryCapacity(movement);

        // Yağmalanacak kaynak yoksa işlem yapmaya gerek yok
        if (totalCarryCapacity <= 0) return;

        // --- 2. Yağmalanabilir Miktarı Belirle (Vulnerability) ---
        // Klanlar mantığı: Kaynakların belli bir yüzdesi (örn: %30) yağmalanabilir.
        // Bu, depo seviyesinden bağımsız olarak uygulanır.
        final double LOOT_VULNERABILITY_RATIO = 0.30;

        double availableWood = defenderResources.getWoodAmount() * LOOT_VULNERABILITY_RATIO;
        double availableMeat = defenderResources.getMeatAmount() * LOOT_VULNERABILITY_RATIO;
        double availableIron = defenderResources.getIronAmount() * LOOT_VULNERABILITY_RATIO;

        // --- 3. Gerçek Ganimeti Hesapla (Min(Capacity, Available)) ---
        // Her kaynak ayrı ayrı kapasite ile sınırlandırılır, toplam kapasite tüm kaynaklar için geçerlidir.
        // Basitlik: Taşıma kapasitesini her kaynak için ayrı ayrı limit olarak kullanırız.

        double finalLootWood = Math.min(totalCarryCapacity, availableWood);
        double finalLootMeat = Math.min(totalCarryCapacity, availableMeat);
        double finalLootIron = Math.min(totalCarryCapacity, availableIron);

        // Not: Gerçekte tüm kaynaklar için toplam kapasite kullanılır,
        // ancak basitlik adına bu ayrım daha anlaşılır.

        // --- 4. Uygulama: Saldırganın hareket entity'sine yükle ---
        movement.setWoodCarried(finalLootWood);
        movement.setMeatCarried(finalLootMeat);
        movement.setIronCarried(finalLootIron);

        // --- 5. Uygulama: Savunucunun kaynaklarını düş ---
        defenderResources.setWoodAmount(defenderResources.getWoodAmount() - finalLootWood);
        defenderResources.setMeatAmount(defenderResources.getMeatAmount() - finalLootMeat);
        defenderResources.setIronAmount(defenderResources.getIronAmount() - finalLootIron);

        // Kaydet (Transactional olduğu için otomatik olur, ama explicite çağırmak iyidir)
        resourcesRepository.save(defenderResources);
    }

    private double calculateTotalCarryCapacity(ArmyMovementEntity movement) {
        UnitAccess access = new UnitAccess(movement);
        double totalCapacity = 0;

        totalCapacity += access.getSpearmen() * GameCalculator.getCarryCapacity(UnitType.SPEARMAN);
        totalCapacity += access.getSwordsmen() * GameCalculator.getCarryCapacity(UnitType.SWORDSMAN);
        totalCapacity += access.getAxemen() * GameCalculator.getCarryCapacity(UnitType.AXEMAN);
        totalCapacity += access.getArchers() * GameCalculator.getCarryCapacity(UnitType.ARCHER);
        totalCapacity += access.getScouts() * GameCalculator.getCarryCapacity(UnitType.SCOUT);
        totalCapacity += access.getLightCavalry() * GameCalculator.getCarryCapacity(UnitType.LIGHT_CAVALRY);
        totalCapacity += access.getHeavyCavalry() * GameCalculator.getCarryCapacity(UnitType.HEAVY_CAVALRY);

        // Kuşatma silahları genelde taşımaz veya çok az taşır.
        totalCapacity += access.getRams() * GameCalculator.getCarryCapacity(UnitType.RAM);
        totalCapacity += access.getCatapults() * GameCalculator.getCarryCapacity(UnitType.CATAPULT);

        return totalCapacity;
    }

    private void createReturnMovement(ArmyMovementEntity survivors) {

        // 1. Yeni hareket objesi oluştur
        ArmyMovementEntity returnMove = new ArmyMovementEntity();

        // 2. Source ve Target'ı ters çevir
        returnMove.setSourceVillage(survivors.getTargetVillage()); // Ayrıldığı yer: Saldırılan köy
        returnMove.setTargetVillage(survivors.getSourceVillage()); // Vardığı yer: Kendi köyü

        returnMove.setType(MovementType.RETURN);
        returnMove.setStartTime(LocalDateTime.now());

        // 3. Mesafe ve Hız Hesaplama (En yavaş birime göre)
        double distance = calculateDistance(
                returnMove.getSourceVillage().getXcoord(),
                returnMove.getSourceVillage().getYcoord(),
                returnMove.getTargetVillage().getXcoord(),
                returnMove.getTargetVillage().getYcoord()
        );

        // Kalan birimler içinde en yavaş olanın hızını bul
        int slowestSpeedInMinutes = findSlowestUnitSpeed(survivors);
        int worldSpeed = returnMove.getTargetVillage().getPlayer().getWorld().getSpeed();

        long durationSeconds = (long) ((distance * slowestSpeedInMinutes * 60) / worldSpeed);
        durationSeconds = Math.max(durationSeconds, 10); // Minimum 10 saniye

        returnMove.setArrivalTime(LocalDateTime.now().plusSeconds(durationSeconds));

        // 4. Askerleri ve Ganimeti Kopyala
        // Kayıp uygulanan askerleri direkt kopyalıyoruz
        copyTroopsAndLoot(survivors, returnMove);

        // 5. Kaydet
        movementRepository.save(returnMove);
    }

    /**
     * İki köy arasındaki mesafeyi hesaplar (MovementService'den kopyalanmıştır).
     */
    private double calculateDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    /**
     * ArmyMovement Entity'si içindeki hayatta kalan askerlerin en yavaşının hızını bulur.
     */
    private int findSlowestUnitSpeed(ArmyMovementEntity movement) {
        // En yüksek dakika/alan değeri = En yavaş birimdir.
        int maxSpeed = 0;

        // Tüm birimler için kontrol
        if (movement.getSpearmen() > 0)
            maxSpeed = Math.max(maxSpeed, GameCalculator.getSpeedInMinutes(UnitType.SPEARMAN));
        if (movement.getSwordsmen() > 0)
            maxSpeed = Math.max(maxSpeed, GameCalculator.getSpeedInMinutes(UnitType.SWORDSMAN));
        if (movement.getAxemen() > 0) maxSpeed = Math.max(maxSpeed, GameCalculator.getSpeedInMinutes(UnitType.AXEMAN));
        if (movement.getArchers() > 0) maxSpeed = Math.max(maxSpeed, GameCalculator.getSpeedInMinutes(UnitType.ARCHER));
        if (movement.getScouts() > 0) maxSpeed = Math.max(maxSpeed, GameCalculator.getSpeedInMinutes(UnitType.SCOUT));
        if (movement.getLightCavalry() > 0)
            maxSpeed = Math.max(maxSpeed, GameCalculator.getSpeedInMinutes(UnitType.LIGHT_CAVALRY));
        if (movement.getHeavyCavalry() > 0)
            maxSpeed = Math.max(maxSpeed, GameCalculator.getSpeedInMinutes(UnitType.HEAVY_CAVALRY));
        if (movement.getRams() > 0) maxSpeed = Math.max(maxSpeed, GameCalculator.getSpeedInMinutes(UnitType.RAM));
        if (movement.getCatapults() > 0)
            maxSpeed = Math.max(maxSpeed, GameCalculator.getSpeedInMinutes(UnitType.CATAPULT));
        if (movement.getConquerors() > 0)
            maxSpeed = Math.max(maxSpeed, GameCalculator.getSpeedInMinutes(UnitType.CONQUEROR));

        return maxSpeed > 0 ? maxSpeed : GameCalculator.getSpeedInMinutes(UnitType.SPEARMAN); // En azından mızraklı hızı
    }

    /**
     * Asker ve ganimetleri bir Movement Entity'den diğerine kopyalar.
     */
    private void copyTroopsAndLoot(ArmyMovementEntity source, ArmyMovementEntity target) {
        // Askerleri kopyala
        target.setSpearmen(source.getSpearmen());
        target.setSwordsmen(source.getSwordsmen());
        target.setAxemen(source.getAxemen());
        target.setArchers(source.getArchers());
        target.setScouts(source.getScouts());
        target.setLightCavalry(source.getLightCavalry());
        target.setHeavyCavalry(source.getHeavyCavalry());
        target.setRams(source.getRams());
        target.setCatapults(source.getCatapults());
        target.setConquerors(source.getConquerors());

        // Ganimeti kopyala
        target.setWoodCarried(source.getWoodCarried());
        target.setMeatCarried(source.getMeatCarried());
        target.setIronCarried(source.getIronCarried());
    }

    private boolean hasSurvivors(ArmyMovementEntity movement) {
        return movement.getSpearmen() > 0 ||
                movement.getSwordsmen() > 0 ||
                movement.getAxemen() > 0 ||
                movement.getArchers() > 0 ||
                movement.getScouts() > 0 ||
                movement.getLightCavalry() > 0 ||
                movement.getHeavyCavalry() > 0 ||
                movement.getRams() > 0 ||
                movement.getCatapults() > 0 ||
                movement.getConquerors() > 0;
    }
}
