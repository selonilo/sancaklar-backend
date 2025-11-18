package com.sc.sancaklar.controller;

import com.sc.sancaklar.model.dto.RecruitRequest;
import com.sc.sancaklar.model.dto.UnitRecruitmentModel;
import com.sc.sancaklar.model.enums.BuildingType;
import com.sc.sancaklar.service.RecruitmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruit")
@RequiredArgsConstructor
public class RecruitmentController {
    private final RecruitmentService recruitmentService;

    @PostMapping
    public ResponseEntity<String> recruitUnits(@RequestBody RecruitRequest request) {
        recruitmentService.recruitUnits(request);
        return ResponseEntity.ok("Asker üretim emri verildi!");
    }

    @GetMapping("/{villageId}/queue/{buildingType}")
    public ResponseEntity<List<UnitRecruitmentModel>> getQueue(
            @PathVariable Long villageId,
            @PathVariable String buildingType) { // String olarak alıp Enum'a çevirmek daha güvenli olabilir

        try {
            BuildingType type = BuildingType.valueOf(buildingType.toUpperCase());
            return ResponseEntity.ok(recruitmentService.getRecruitmentQueue(villageId, type));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Geçersiz bina tipi! (BARRACKS, STABLE, WORKSHOP, ACADEMY)");
        }
    }
}
