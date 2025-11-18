package com.sc.sancaklar.model.data;

import com.sc.sancaklar.model.enums.BuildingType;

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
            case HEADQUARTERS: // Ana Bina
                baseWood = 200;
                baseMeat = 150;
                baseIron = 100;
                break;

            case WAREHOUSE:    // Depo (Erken oyun için ucuz olmalı)
                baseWood = 60;
                baseMeat = 50;
                baseIron = 40;
                break;

            case FARM:         // Çiftlik
                baseWood = 45;
                baseMeat = 40;
                baseIron = 30;
                break;

            // --- KAYNAK ÜRETİM ---
            case TIMBER_CAMP:  // Oduncu
                baseWood = 50;
                baseMeat = 60;
                baseIron = 40;
                break;

            case MEAT_PLANT:   // Et Tesisi (Kil Ocağı muadili)
                baseWood = 65;
                baseMeat = 50;
                baseIron = 40;
                break;

            case IRON_MINE:    // Demir Madeni (Biraz daha pahalıdır)
                baseWood = 75;
                baseMeat = 65;
                baseIron = 70;
                break;

            // --- ASKERİ BİNALAR ---
            case BARRACKS:     // Kışla
                baseWood = 200;
                baseMeat = 100;
                baseIron = 80;
                break;

            case STABLE:       // Ahır (Atlılar için, daha pahalı)
                baseWood = 270;
                baseMeat = 240;
                baseIron = 260;
                break;

            case WORKSHOP:     // Atölye (Kuşatma silahları)
                baseWood = 300;
                baseMeat = 240;
                baseIron = 260;
                break;

            case SMITHY:       // Demirci (Araştırmalar için)
                baseWood = 220;
                baseMeat = 180;
                baseIron = 240;
                break;

            case WALL:         // Sur (Genelde taş/et ister)
                baseWood = 50;
                baseMeat = 100;
                baseIron = 20;
                break;

            // --- EKONOMİ VE STRATEJİ ---
            case MARKET:       // Pazar
                baseWood = 100;
                baseMeat = 100;
                baseIron = 100;
                break;

            case ACADEMY:      // Akademi (Oyunun en pahalı binası)
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
            case ACADEMY -> 80; // Oyunun en değerli binası
            case HEADQUARTERS -> 15; // Ana bina seviyesi önemlidir
            case SMITHY -> 12; // Teknoloji binası değerlidir

            // --- TIER 2: İLERİ ASKERİYE (Yüksek Puan) ---
            case WORKSHOP -> 10; // Kuşatma silahları komplekstir
            case STABLE -> 8; // Ahır pahalıdır, puanı iyidir

            // --- TIER 3: TEMEL ASKERİYE VE SAVUNMA (Orta Puan) ---
            case BARRACKS -> 6;
            case WALL -> 6; // Sur stratejiktir

            // --- TIER 4: EKONOMİ (Orta Puan) ---
            case MARKET -> 5;
            case IRON_MINE -> 4; // Demir genelde daha değerlidir
            case MEAT_PLANT -> 4;
            case TIMBER_CAMP -> 4;

            // --- TIER 5: ALTYAPI (Düşük Puan - Sürümden Kazanır) ---
            case WAREHOUSE -> 3;
            case FARM -> 2; // Çiftlik çok seviyelidir (30), puanı düşük tutulur

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
}
