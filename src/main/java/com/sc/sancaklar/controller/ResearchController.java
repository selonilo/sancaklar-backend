package com.sc.sancaklar.controller;

import com.sc.sancaklar.model.dto.*;
import com.sc.sancaklar.service.ResearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/research")
@RequiredArgsConstructor
@Tag(name = "Demirci ve Araştırma", description = "Asker teknolojilerini araştırma ve seviyelerini görüntüleme işlemleri.")
public class ResearchController {
    private final ResearchService researchService;

    @Operation(
            summary = "Araştırma Emri Başlat",
            description = "Belirtilen köyde, istenen birim için (Örn: SWORDSMAN) araştırma sürecini başlatır. Gereksinim ve kaynak kontrolü yapılır."
    )
    @PostMapping("/start")
    public ResponseEntity<String> startResearch(@RequestBody ResearchRequest request) {
        researchService.startResearch(request.getVillageId(), request.getUnitType());
        return ResponseEntity.ok("Araştırma emri başarıyla verildi!");
    }

    @Operation(
            summary = "Teknoloji Seviyelerini Getir",
            description = "Köyde **halihazırda açılmış** olan tüm birim teknolojilerinin durumunu (0: Kilitli, 1: Açık) listeler. Kışla'daki asker üretim butonlarını aktif etmek için kullanılır."
    )
    @GetMapping("/{villageId}/levels")
    public ResponseEntity<VillageResearchesModel> getResearchLevels(
            @Parameter(description = "Seviyesi istenen köyün ID'si", example = "1")
            @PathVariable Long villageId) {

        return ResponseEntity.ok(researchService.getResearches(villageId));
    }

    @Operation(
            summary = "Araştırma Kuyruğunu Görüntüle",
            description = "Demircide devam eden araştırma emirlerini listeler. Frontend'deki geri sayım sayaçları (timer) bu veriyi kullanır."
    )
    @GetMapping("/{villageId}/queue")
    public ResponseEntity<List<ResearchQueueModel>> getQueue(
            @Parameter(description = "Kuyruğu istenen köyün ID'si", example = "1")
            @PathVariable Long villageId) {

        return ResponseEntity.ok(researchService.getResearchQueue(villageId));
    }
}
