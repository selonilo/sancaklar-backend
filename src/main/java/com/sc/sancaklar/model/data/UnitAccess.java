package com.sc.sancaklar.model.data;

import com.sc.sancaklar.model.entity.ArmyMovementEntity;
import com.sc.sancaklar.model.entity.StationedTroopsEntity;
import com.sc.sancaklar.model.entity.VillageTroopsEntity;

/**
 * Helper sınıfı: ArmyMovement, VillageTroops ve StationedTroops Entity'leri
 * arasındaki farkı ortadan kaldırarak birim sayılarına tek bir yoldan erişimi sağlar.
 */
public class UnitAccess { // static olmalı ki dış sınıfa bağlı olmadan var olabilsin

    // Dahili depolama alanları
    private final int spearmen;
    private final int swordsmen;
    private final int axemen;
    private final int archers;
    private final int scouts;
    private final int lightCavalry;
    private final int heavyCavalry;
    private final int rams;
    private final int catapults;
    private final int conquerors;

    public UnitAccess(Object units) {
        if (units instanceof ArmyMovementEntity) {
            ArmyMovementEntity a = (ArmyMovementEntity) units;
            this.spearmen = a.getSpearmen();
            this.swordsmen = a.getSwordsmen();
            this.axemen = a.getAxemen();
            this.archers = a.getArchers();
            this.scouts = a.getScouts();
            this.lightCavalry = a.getLightCavalry();
            this.heavyCavalry = a.getHeavyCavalry();
            this.rams = a.getRams();
            this.catapults = a.getCatapults();
            this.conquerors = a.getConquerors();
        } else if (units instanceof VillageTroopsEntity) {
            VillageTroopsEntity v = (VillageTroopsEntity) units;
            this.spearmen = v.getSpearman();
            this.swordsmen = v.getSwordsman();
            this.axemen = v.getAxeman();
            this.archers = v.getArcher();
            this.scouts = v.getScout();
            this.lightCavalry = v.getLightCavalry();
            this.heavyCavalry = v.getHeavyCavalry();
            this.rams = v.getRam();
            this.catapults = v.getCatapult();
            this.conquerors = v.getConqueror();
        } else if (units instanceof StationedTroopsEntity) {
            StationedTroopsEntity s = (StationedTroopsEntity) units;
            this.spearmen = s.getSpearmen();
            this.swordsmen = s.getSwordsmen();
            this.axemen = s.getAxemen();
            this.archers = s.getArchers();
            this.scouts = s.getScouts();
            this.lightCavalry = s.getLightCavalry();
            this.heavyCavalry = s.getHeavyCavalry();
            this.rams = s.getRams();
            this.catapults = s.getCatapults();
            this.conquerors = s.getConquerors();
        } else {
            // Varsayılan: Boş ordu
            this.spearmen = 0; this.swordsmen = 0; this.axemen = 0; this.archers = 0;
            this.scouts = 0; this.lightCavalry = 0; this.heavyCavalry = 0;
            this.rams = 0; this.catapults = 0; this.conquerors = 0;
        }
    }

    // --- PUBLIC GETTER METODLARI (calculateTotalPower'ın kullanacağı) ---
    public int getSpearmen() { return spearmen; }
    public int getSwordsmen() { return swordsmen; }
    public int getAxemen() { return axemen; }
    public int getArchers() { return archers; }
    public int getScouts() { return scouts; }
    public int getLightCavalry() { return lightCavalry; }
    public int getHeavyCavalry() { return heavyCavalry; }
    public int getRams() { return rams; }
    public int getCatapults() { return catapults; }
    public int getConquerors() { return conquerors; }
}