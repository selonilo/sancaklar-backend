package com.sc.sancaklar.model.data;

import com.sc.sancaklar.model.entity.VillageBuildingsEntity;
import com.sc.sancaklar.model.enums.BuildingType;
import com.sc.sancaklar.model.enums.UnitType;

public class GameCalculator {
    // Temel üretim değerleri (Seviye 0 iken bile azıcık üretim olsun)
    private static final int BASE_PRODUCTION = 30;

    // Üretim artış katsayısı (Her seviyede %20 artış)
    private static final double PRODUCTION_FACTOR = 1.2;

    // Depo kapasitesi formülü
    private static final int BASE_CAPACITY = 1000;
    private static final double CAPACITY_FACTOR = 1.25;

    /**
     * Belirli bir bina seviyesi ve dünya hızı için SAATLİK üretimi hesaplar.
     */
    public static int calculateHourlyProduction(int level, int worldSpeed) {
        if (level == 0) return BASE_PRODUCTION * worldSpeed; // Bina yoksa bile temel üretim

        // Formül: 30 * (1.2 ^ Level) * Dünya Hızı
        double production = BASE_PRODUCTION * Math.pow(PRODUCTION_FACTOR, level);
        return (int) (production * worldSpeed);
    }

    /**
     * Depo seviyesine göre maksimum kapasiteyi hesaplar.
     */
    public static int calculateWarehouseCapacity(int level) {
        // Formül: 1000 * (1.25 ^ Level)
        return (int) (BASE_CAPACITY * Math.pow(CAPACITY_FACTOR, level));
    }

    /**
     * Bir binanın sonraki seviyesi için gereken kaynağı hesaplar.
     * Formül: BaseCost * (1.2 ^ Level)
     */
    public static int calculateBuildingCost(BuildingType type, int currentLevel, String resourceType) {
        int baseWood = 0, baseMeat = 0, baseIron = 0;

        // Her bina için taban maliyetler (Örnek)
        switch (type) {
            // --- ANA BİNALAR ---
            case headquarters: // Ana Bina
                baseWood = 200;
                baseMeat = 150;
                baseIron = 100;
                break;

            case warehouse:    // Depo (Erken oyun için ucuz olmalı)
                baseWood = 60;
                baseMeat = 50;
                baseIron = 40;
                break;

            case farm:         // Çiftlik
                baseWood = 45;
                baseMeat = 40;
                baseIron = 30;
                break;

            // --- KAYNAK ÜRETİM ---
            case timberCamp:  // Oduncu
                baseWood = 50;
                baseMeat = 60;
                baseIron = 40;
                break;

            case meatPlant:   // Et Tesisi (Kil Ocağı muadili)
                baseWood = 65;
                baseMeat = 50;
                baseIron = 40;
                break;

            case ironMine:    // Demir Madeni (Biraz daha pahalıdır)
                baseWood = 75;
                baseMeat = 65;
                baseIron = 70;
                break;

            // --- ASKERİ BİNALAR ---
            case barracks:     // Kışla
                baseWood = 200;
                baseMeat = 100;
                baseIron = 80;
                break;

            case stable:       // Ahır (Atlılar için, daha pahalı)
                baseWood = 270;
                baseMeat = 240;
                baseIron = 260;
                break;

            case workshop:     // Atölye (Kuşatma silahları)
                baseWood = 300;
                baseMeat = 240;
                baseIron = 260;
                break;

            case smithy:       // Demirci (Araştırmalar için)
                baseWood = 220;
                baseMeat = 180;
                baseIron = 240;
                break;

            case wall:         // Sur (Genelde taş/et ister)
                baseWood = 50;
                baseMeat = 100;
                baseIron = 20;
                break;

            // --- EKONOMİ VE STRATEJİ ---
            case market:       // Pazar
                baseWood = 100;
                baseMeat = 100;
                baseIron = 100;
                break;

            case academy:      // Akademi (Oyunun en pahalı binası)
                // Not: Bu base cost. Seviye çarpanı olacağı için 15.000 yerine
                // formüle uygun bir taban fiyat belirledik.
                // Formülde (1.2 ^ level) ile çarpılınca gerçek değerini bulacak.
                baseWood = 8000;
                baseMeat = 10000;
                baseIron = 5000;
                break;

            default: // Hata durumunda varsayılan
                baseWood = 100;
                baseMeat = 100;
                baseIron = 100;
                break;
        }

        double factor = 1.20; // Her seviyede maliyet %20 artar
        double multiplier = Math.pow(factor, currentLevel);

        if (resourceType.equals("WOOD")) return (int) (baseWood * multiplier);
        if (resourceType.equals("MEAT")) return (int) (baseMeat * multiplier);
        if (resourceType.equals("IRON")) return (int) (baseIron * multiplier);
        return 0;
    }

    /**
     * İnşaat süresini hesaplar (Saniye cinsinden).
     * Ana Bina seviyesi arttıkça süre kısalır.
     */
    public static long calculateConstructionTimeInSeconds(BuildingType type, int currentLevel, int hqLevel, int worldSpeed) {
        // Taban süre (Örn: 15 dakika = 900 saniye)
        int baseSeconds = 900;

        // Bina seviyesi arttıkça süre uzar
        double levelFactor = Math.pow(1.18, currentLevel);

        // Ana Bina (HQ) seviyesi arttıkça süre kısalır (HQ başına %5 hız)
        double hqReduction = 1 + (hqLevel * 0.05);

        // Formül: (Taban * SeviyeÇarpanı) / (AnaBinaEtkisi * DünyaHızı)
        double result = (baseSeconds * levelFactor) / (hqReduction * worldSpeed);

        return (long) Math.max(result, 10); // En az 10 saniye sürsün
    }

    public static int calculateBuildingPoints(BuildingType type, int level) {
        int multiplier = switch (type) {
            // --- TIER 1: PRESTİJ VE YÖNETİM (En Yüksek Puan) ---
            case academy -> 80; // Oyunun en değerli binası
            case headquarters -> 15; // Ana bina seviyesi önemlidir
            case smithy -> 12; // Teknoloji binası değerlidir

            // --- TIER 2: İLERİ ASKERİYE (Yüksek Puan) ---
            case workshop -> 10; // Kuşatma silahları komplekstir
            case stable -> 8; // Ahır pahalıdır, puanı iyidir

            // --- TIER 3: TEMEL ASKERİYE VE SAVUNMA (Orta Puan) ---
            case barracks -> 6;
            case wall -> 6; // Sur stratejiktir

            // --- TIER 4: EKONOMİ (Orta Puan) ---
            case market -> 5;
            case ironMine -> 4; // Demir genelde daha değerlidir
            case meatPlant -> 4;
            case timberCamp -> 4;

            // --- TIER 5: ALTYAPI (Düşük Puan - Sürümden Kazanır) ---
            case warehouse -> 3;
            case farm -> 2; // Çiftlik çok seviyelidir (30), puanı düşük tutulur

            default -> 3;
        };

        // --- HESAPLAMA MANTIĞI ---
        // Basit "Level * Çarpan" yerine, seviye arttıkça puanın katlanarak artmasını sağlıyoruz.
        // Örn: 1. Seviye -> 1x puan verirken, 20. Seviye -> Daha bonkör olmalı.

        // Math.pow(level, 1.1) -> Seviye arttıkça çarpanın etkisini hafifçe artırır (Kavisli artış).
        // Örnek:
        // Farm (Carpan 2): Lv 10 -> 25 Puan ekler.
        // Academy (Carpan 80): Lv 1 -> 80 Puan ekler.

        return (int) (multiplier * Math.pow(level, 1.1));
    }


    // Araştırma Maliyeti
    public static int[] getResearchCost(UnitType unit) {
        // Sırasıyla: Odun, Et, Demir
        return switch (unit) {
            // --- Piyadeler ---
            case SPEARMAN -> new int[]{100, 100, 100};      // Başlangıç birimi
            case SWORDSMAN -> new int[]{500, 400, 300};     // Savunma odaklı
            case AXEMAN -> new int[]{700, 600, 500};        // Saldırı odaklı
            case ARCHER -> new int[]{600, 300, 400};        // Yay için Odun ağırlıklı

            // --- Atlılar ve Casus ---
            case SCOUT -> new int[]{200, 500, 200};         // Hızlı, fazla et (erzak) yer
            case LIGHT_CAVALRY -> new int[]{1200, 1000, 800}; // Hızlı yağma
            case HEAVY_CAVALRY -> new int[]{2500, 2000, 3000}; // Zırh için çok Demir gerekir

            // --- Kuşatma ---
            case RAM -> new int[]{1200, 800, 1200};         // Odun ve Demir dengeli
            case CATAPULT -> new int[]{1600, 1200, 1500};   // Mancınık daha pahalı

            // --- Özel ---
            case CONQUEROR -> new int[]{15000, 20000, 15000}; // Oyun sonu birimi (Misyoner/Asilzade)

            // Hata durumunda varsayılan (veya exception fırlatılabilir)
            default -> new int[]{500, 500, 500};
        };
    }

    public static long getResearchTime(UnitType unit, int smithyLevel, int worldSpeed) {
        // Saniye cinsinden baz süreler (Smithy Lvl 0 varsayımıyla)
        int baseSeconds = switch (unit) {
            // --- Tier 1: Piyadeler ---
            case SPEARMAN -> 300;         // 5 dk
            case SWORDSMAN -> 900;        // 15 dk
            case AXEMAN -> 1200;          // 20 dk (Saldırı birimi biraz daha uzun)
            case ARCHER -> 1200;          // 20 dk

            // --- Tier 2: Atlılar ve Casus ---
            case SCOUT -> 900;            // 15 dk (Erken oyun için hızlı erişim)
            case LIGHT_CAVALRY -> 2400;   // 40 dk
            case HEAVY_CAVALRY -> 3600;   // 60 dk (1 saat)

            // --- Tier 3: Kuşatma ---
            case RAM -> 2700;             // 45 dk
            case CATAPULT -> 4800;        // 80 dk (1 saat 20 dk)

            // --- Tier 4: Özel ---
            case CONQUEROR -> 10800;
            default -> 1;// 180 dk (3 saat)
        };

        // Demirci seviyesi arttıkça araştırma hızlanır
        // Örnek: Lvl 20 için -> 1 + (20 * 0.05) = 2 (Süre yarıya iner)
        double reduction = 1 + (smithyLevel * 0.05);

        // Hesaplama ve 0'a bölünme/sıfır çıkma riskine karşı koruma (en az 1 sn)
        long finalTime = (long) ((baseSeconds / reduction) / worldSpeed);
        return Math.max(1, finalTime);
    }

    // Gereksinim Kontrolü (Bina seviyeleri yetiyor mu?)
    public static boolean isResearchRequirementsMet(UnitType unit, VillageBuildingsEntity buildings) {
        return switch (unit) {
            case SPEARMAN -> buildings.getSmithy() >= 1; // Demirci 1 yeterli
            case SWORDSMAN -> buildings.getSmithy() >= 3;
            case AXEMAN -> buildings.getSmithy() >= 5;
            case LIGHT_CAVALRY -> buildings.getStable() >= 3 && buildings.getSmithy() >= 10;
            case CONQUEROR -> buildings.getAcademy() >= 1;
            default -> true;
        };
    }

    public static int[] getUnitCost(UnitType type) {
        // Sırasıyla: Odun, Et, Demir
        return switch (type) {
            // --- Piyadeler (Kışla) ---
            case SPEARMAN -> new int[]{50, 30, 10};       // En ucuz, temel birim
            case SWORDSMAN -> new int[]{30, 30, 70};      // Demir ağırlıklı (Zırh)
            case AXEMAN -> new int[]{60, 30, 40};         // Dengeli saldırı birimi
            case ARCHER -> new int[]{100, 30, 60};        // Odun ağırlıklı (Yay)

            // --- Atlılar ve Casus (Ahır) ---
            case SCOUT -> new int[]{50, 50, 20};          // Ucuz, hızlı üretim
            case LIGHT_CAVALRY -> new int[]{125, 100, 250}; // Hızlı, demir ister
            case HEAVY_CAVALRY -> new int[]{200, 150, 600}; // Çok zırhlı, çok demir

            // --- Kuşatma (Atölye) ---
            case RAM -> new int[]{300, 200, 200};         // Sur yıkıcı
            case CATAPULT -> new int[]{320, 400, 100};    // Bina yıkıcı (Et/Kil pahalı)

            // --- Özel (Akademi) ---
            case CONQUEROR -> new int[]{40000, 50000, 50000}; // Oyun sonu, çok pahalı

            // Hata önleme
            default -> new int[]{100, 100, 100};
        };
    }

    // Birim Başına Nüfus (Çiftlikte kapladığı yer)
    public static int getUnitPopulation(UnitType type) {
        return switch (type) {
            case LIGHT_CAVALRY -> 4;
            case HEAVY_CAVALRY -> 6;
            case RAM -> 5;
            case CATAPULT -> 8;
            case CONQUEROR -> 100;
            default -> 1; // Piyadeler 1 yer kaplar
        };
    }

    // Birim Başına Üretim Süresi (Saniye)
    public static long getUnitProductionTime(UnitType type, int buildingLevel, int worldSpeed) {
        double baseSeconds = switch (type) {
            case SPEARMAN -> 100; // Başlangıç hızı
            case SWORDSMAN -> 150;
            case AXEMAN -> 180;
            case CONQUEROR -> 10000;
            default -> 200;
        };

        // Bina seviyesi arttıkça üretim hızlanır (Her seviyede %5 civarı)
        // Formül: TabanSüre * (1.05 ^ -Level)
        double reduction = Math.pow(1.05, -buildingLevel); // Seviye arttıkça süre azalır

        // Dünya hızı da etkiler
        return (long) Math.max(1, (baseSeconds * reduction) / worldSpeed);
    }

    // Bu birim hangi binada üretilir?
    public static BuildingType getProductionBuilding(UnitType type) {
        return switch (type) {
            case SCOUT, LIGHT_CAVALRY, HEAVY_CAVALRY -> BuildingType.stable;
            case RAM, CATAPULT -> BuildingType.workshop;
            case CONQUEROR -> BuildingType.academy; // Veya Saray
            default -> BuildingType.barracks; // Piyadeler
        };
    }

    // 1. Saldırı Gücü
    public static int getAttackPower(UnitType type) {
        return switch (type) {
            case SPEARMAN -> 10;
            case SWORDSMAN -> 25;
            case AXEMAN -> 40; // Tam saldırı birimi
            case ARCHER -> 15;
            case LIGHT_CAVALRY -> 130;
            case HEAVY_CAVALRY -> 150;
            case RAM -> 2;
            case CONQUEROR -> 30;
            default -> 0;
        };
    }

    // 2. Savunma Gücü (Genel, Atlıya Karşı, Okçuya Karşı ortalaması basitleştirildi)
    public static int getDefensePower(UnitType type) {
        return switch (type) {
            case SPEARMAN -> 35; // Atlılara karşı iyidir (Basitleştirilmiş genel defans)
            case SWORDSMAN -> 50; // Genel defans
            case AXEMAN -> 10;    // Savunması kötüdür
            case ARCHER -> 40;
            case LIGHT_CAVALRY -> 30;
            case HEAVY_CAVALRY -> 200; // Tank
            case WALL -> 0; // Sur bina olduğu için formülü ayrıdır
            default -> 10;
        };
    }

    // 3. Ganimet Taşıma Kapasitesi
    public static int getCarryCapacity(UnitType type) {
        return switch (type) {
            case SPEARMAN -> 25;
            case SWORDSMAN -> 15;
            case AXEMAN -> 10;
            case LIGHT_CAVALRY -> 80; // Atlılar çok taşır
            case HEAVY_CAVALRY -> 50;
            default -> 0;
        };
    }

    // 4. Hız (Dakika / Kare) - Haritada hareket süresi için
    public static int getSpeedInMinutes(UnitType type) {
        return switch (type) {
            case SCOUT -> 9; // En hızlı
            case LIGHT_CAVALRY -> 10;
            case HEAVY_CAVALRY -> 11;
            case AXEMAN -> 18;
            case SWORDSMAN -> 22;
            case RAM -> 30; // En yavaş (Ordu en yavaş birime göre gider)
            case CONQUEROR -> 35;
            default -> 18;
        };
    }
}
