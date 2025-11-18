package com.sc.sancaklar.service;

import com.sc.sancaklar.model.data.GameCalculator;
import com.sc.sancaklar.model.entity.VillageBuildingsEntity;
import com.sc.sancaklar.model.entity.VillageEntity;
import com.sc.sancaklar.model.entity.VillageResourcesEntity;
import com.sc.sancaklar.repository.VillageResourcesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    private final VillageResourcesRepository resourceRepository;

    /**
     * Bir köyün kaynaklarını şu anki zamana göre günceller.
     * Bu metod, köyle ilgili herhangi bir işlem yapılmadan önce MUTLAKA çağrılmalıdır.
     */
    @Transactional
    public void calculateAndUpdateResources(VillageEntity village) {
        VillageResourcesEntity resources = village.getResources();
        VillageBuildingsEntity buildings = village.getBuildings();
        int worldSpeed = village.getPlayer().getWorld().getSpeed();

        // 1. Geçen süreyi saniye cinsinden bul
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastUpdated = resources.getLastUpdated();

        if (lastUpdated == null) {
            lastUpdated = now; // İlk oluşumda null gelirse
        }

        long secondsPassed = Duration.between(lastUpdated, now).getSeconds();

        // Eğer çok kısa süre geçtiyse (örn: salise) işlem yapma
        if (secondsPassed < 1) return;

        // 2. Saatlik üretim hızlarını hesapla (Level ve Hız bazlı)
        int woodPerHour = GameCalculator.calculateHourlyProduction(buildings.getTimberCamp(), worldSpeed);
        int meatPerHour = GameCalculator.calculateHourlyProduction(buildings.getMeatProduction(), worldSpeed); // Et
        int ironPerHour = GameCalculator.calculateHourlyProduction(buildings.getIronMine(), worldSpeed);

        // 3. Geçen sürede ne kadar üretildiğini bul
        // Formül: (Saatlik Üretim / 3600) * Geçen Saniye
        double producedWood = (woodPerHour / 3600.0) * secondsPassed;
        double producedMeat = (meatPerHour / 3600.0) * secondsPassed;
        double producedIron = (ironPerHour / 3600.0) * secondsPassed;

        // 4. Depo kapasitesini hesapla
        int maxCapacity = GameCalculator.calculateWarehouseCapacity(buildings.getWarehouse());

        // 5. Yeni miktarları set et (Kapasiteyi aşamaz!)
        resources.setWoodAmount(Math.min(resources.getWoodAmount() + producedWood, maxCapacity));
        resources.setMeatAmount(Math.min(resources.getMeatAmount() + producedMeat, maxCapacity));
        resources.setIronAmount(Math.min(resources.getIronAmount() + producedIron, maxCapacity));

        // 6. Zamanı güncelle ve kaydet
        resources.setLastUpdated(now);
        resourceRepository.save(resources);
    }
}
