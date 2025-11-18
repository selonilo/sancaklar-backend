package com.sc.sancaklar.controller;

import com.sc.sancaklar.model.dto.ConstructionModel;
import com.sc.sancaklar.model.dto.PlayerModel;
import com.sc.sancaklar.model.dto.UpgradeBuildingRequest;
import com.sc.sancaklar.model.dto.village.VillageModel;
import com.sc.sancaklar.service.VillageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/village")
@RequiredArgsConstructor
public class VillageController {
    private final VillageService villageService;

    // GET: Köy detaylarını getir (Kaynakları güncelleyerek)
    @GetMapping("/{villageId}")
    public ResponseEntity<VillageModel> getVillage(@PathVariable Long villageId) {
        VillageModel village = villageService.getVillageById(villageId);
        return ResponseEntity.ok(village);
    }

    @PostMapping("/build")
    public ResponseEntity<String> upgradeBuilding(@RequestBody UpgradeBuildingRequest request) {
        villageService.upgradeBuilding(request);
        return ResponseEntity.ok("İnşaat emri başarıyla verildi!");
    }

    @GetMapping("/constructions/{villageId}")
    public ResponseEntity<List<ConstructionModel>> getConstructions(@PathVariable Long villageId) {
        List<ConstructionModel> queue = villageService.getConstructionQueue(villageId);
        return ResponseEntity.ok(queue);
    }
}
