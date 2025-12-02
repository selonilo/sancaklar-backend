package com.sc.sancaklar.model.mapper;

import com.sc.sancaklar.model.data.GameCalculator;
import com.sc.sancaklar.model.dto.VillageResearchesModel;
import com.sc.sancaklar.model.dto.village.VillageBuildingsModel;
import com.sc.sancaklar.model.dto.village.VillageModel;
import com.sc.sancaklar.model.dto.village.VillageResourcesModel;
import com.sc.sancaklar.model.dto.village.VillageTroopsModel;
import com.sc.sancaklar.model.entity.*;
import org.springframework.stereotype.Component;

@Component
public class VillageConverter {

    public VillageModel toModel(VillageEntity entity) {
        if (entity == null) return null;

        VillageModel model = new VillageModel();

        // Temel Bilgiler
        model.setId(entity.getId());
        model.setName(entity.getName());
        model.setXCoord(entity.getXcoord());
        model.setYCoord(entity.getYcoord());
        model.setPoints(entity.getPoints());
        model.setLoyalty(entity.getLoyalty());

        // Oyuncu Bilgisi (Null check önemli)
        if (entity.getPlayer() != null) {
            model.setPlayerId(entity.getPlayer().getId());
            if (entity.getPlayer().getUser() != null) {
                model.setPlayerName(entity.getPlayer().getUser().getUsername());
            }
        }

        // --- KRİTİK NOKTA: Dünya Hızını Bulma ---
        // Üretim hızlarını hesaplamak için dünyanın hızına (1x, 2x vb.) ihtiyacımız var.
        int worldSpeed = 1; // Varsayılan
        if (entity.getPlayer() != null && entity.getPlayer().getWorld() != null) {
            worldSpeed = entity.getPlayer().getWorld().getSpeed();
        }

        // Alt Modelleri Dönüştür
        model.setBuildings(toBuildingsModel(entity.getBuildings()));

        // Resources modeline artık 'worldSpeed' bilgisini de gönderiyoruz
        model.setResources(toResourcesModel(entity.getResources(), entity.getBuildings(), worldSpeed));

        model.setTroops(toTroopsModel(entity.getTroops()));

        model.setResearches(toResearchesModel(entity.getResearches()));

        return model;
    }

    private VillageBuildingsModel toBuildingsModel(VillageBuildingsEntity entity) {
        if (entity == null) return new VillageBuildingsModel();
        VillageBuildingsModel model = new VillageBuildingsModel();
        // Tüm alanları eşle (Lombok varsa Mapper kütüphanesi de kullanılabilir ama manuel daha güvenli şu an)
        model.setHeadquarters(entity.getHeadquarters());
        model.setBarracks(entity.getBarracks());
        model.setStable(entity.getStable());
        model.setWorkshop(entity.getWorkshop());
        model.setAcademy(entity.getAcademy());
        model.setSmithy(entity.getSmithy());
        model.setMarket(entity.getMarket());
        model.setTimberCamp(entity.getTimberCamp());
        model.setMeatPlant(entity.getMeatPlant());
        model.setIronMine(entity.getIronMine());
        model.setFarm(entity.getFarm());
        model.setWarehouse(entity.getWarehouse());
        model.setWall(entity.getWall());
        return model;
    }

    private VillageResourcesModel toResourcesModel(VillageResourcesEntity resEntity,
                                                   VillageBuildingsEntity buildEntity,
                                                   int worldSpeed) {
        if (resEntity == null) return new VillageResourcesModel();

        VillageResourcesModel model = new VillageResourcesModel();
        model.setWoodAmount(resEntity.getWoodAmount());
        model.setMeatAmount(resEntity.getMeatAmount());
        model.setIronAmount(resEntity.getIronAmount());
        model.setLastUpdated(resEntity.getLastUpdated());

        if (buildEntity != null) {
            // Depo Kapasitesi
            model.setStorageCapacity(GameCalculator.calculateWarehouseCapacity(buildEntity.getWarehouse()));

            // --- SAATLİK ÜRETİM HIZLARINI HESAPLA VE EKLE ---
            model.setWoodHourlyProduction(GameCalculator.calculateHourlyProduction(buildEntity.getTimberCamp(), worldSpeed));
            model.setMeatHourlyProduction(GameCalculator.calculateHourlyProduction(buildEntity.getMeatPlant(), worldSpeed));
            model.setIronHourlyProduction(GameCalculator.calculateHourlyProduction(buildEntity.getIronMine(), worldSpeed));
        }

        return model;
    }

    private VillageTroopsModel toTroopsModel(VillageTroopsEntity entity) {
        if (entity == null) return new VillageTroopsModel();
        VillageTroopsModel model = new VillageTroopsModel();
        model.setSpearmen(entity.getSpearmen());
        model.setSwordsmen(entity.getSwordsmen());
        model.setAxemen(entity.getAxemen());
        model.setArchers(entity.getArchers());
        model.setScouts(entity.getScouts());
        model.setLightCavalry(entity.getLightCavalry());
        model.setHeavyCavalry(entity.getHeavyCavalry());
        model.setRams(entity.getRams());
        model.setCatapults(entity.getCatapults());
        model.setConquerors(entity.getConquerors());
        return model;
    }

    private VillageResearchesModel toResearchesModel(VillageResearchesEntity entity) {
        if (entity == null) return new VillageResearchesModel(); // Boş dön, null patlamasın

        VillageResearchesModel model = new VillageResearchesModel();
        model.setSpearmen(entity.getSpearmen());
        model.setSwordsmen(entity.getSwordsmen());
        model.setAxemen(entity.getAxemen());
        model.setArchers(entity.getArchers());

        model.setScouts(entity.getScouts());
        model.setLightCavalry(entity.getLightCavalry());
        model.setHeavyCavalry(entity.getHeavyCavalry());

        model.setRams(entity.getRams());
        model.setCatapults(entity.getCatapults());
        model.setConquerors(entity.getConquerors());

        return model;
    }
}