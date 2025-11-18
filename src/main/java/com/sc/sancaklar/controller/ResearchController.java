package com.sc.sancaklar.controller;

import com.sc.sancaklar.model.dto.*;
import com.sc.sancaklar.model.dto.village.VillageModel;
import com.sc.sancaklar.service.ResearchService;
import com.sc.sancaklar.service.WorldService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/research")
@RequiredArgsConstructor
public class ResearchController {
    private final ResearchService researchService;

    @PostMapping("/start")
    public ResponseEntity<String> startResearch(@RequestBody ResearchRequest request) {
        researchService.startResearch(request.getVillageId(), request.getUnitType());
        return ResponseEntity.ok("Araştırma emri başarıyla verildi!");
    }

    // GET: Hangi askerler açık? (Demirci ekranı açılınca bu çağrılır)
    @GetMapping("/{villageId}/levels")
    public ResponseEntity<VillageResearchesModel> getResearchLevels(@PathVariable Long villageId) {
        return ResponseEntity.ok(researchService.getResearches(villageId));
    }

    // GET: Şu an ne araştırılıyor? (Geri sayım sayacı için)
    @GetMapping("/{villageId}/queue")
    public ResponseEntity<List<ResearchQueueModel>> getQueue(@PathVariable Long villageId) {
        return ResponseEntity.ok(researchService.getResearchQueue(villageId));
    }
}
