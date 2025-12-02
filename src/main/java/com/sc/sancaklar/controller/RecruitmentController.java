package com.sc.sancaklar.controller;

import com.sc.sancaklar.model.dto.RecruitRequest;
import com.sc.sancaklar.model.dto.UnitRecruitmentModel;
import com.sc.sancaklar.model.enums.BuildingType;
import com.sc.sancaklar.service.RecruitmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruit")
@RequiredArgsConstructor
@Tag(name = "Asker Üretimi ve Kuyruk", description = "Kışla, Ahır ve Atölye'de birim üretimi başlatma ve kuyruk takibi işlemleri.")
public class RecruitmentController {
    private final RecruitmentService recruitmentService;

    @Operation(
            summary = "Asker Üretim Emri Ver",
            description = "Belirtilen köyde, istenen askeri birimi (**unitType**) belirtilen miktarda (**amount**) üretmek üzere kuyruğu başlatır. Kaynak, Nüfus ve Araştırma (Tech) kontrolleri yapılır."
    )
    @PostMapping
    public ResponseEntity<String> recruitUnits(@RequestBody RecruitRequest request) {
        recruitmentService.recruitUnits(request);
        return ResponseEntity.ok("Asker üretim emri verildi!");
    }

    @Operation(
            summary = "Üretim Kuyruğunu Görüntüle",
            description = "Belirtilen üretim binasına (**BARRACKS, STABLE, WORKSHOP**) ait devam eden asker üretim emirlerini (kaç asker, ne zaman bitecek) listeler."
    )
    @GetMapping("/{villageId}/queue/{buildingType}")
    public ResponseEntity<List<UnitRecruitmentModel>> getQueue(
            @Parameter(description = "Kuyruğu istenen köyün ID'si", example = "1")
            @PathVariable Long villageId,

            @Parameter(description = "Üretim binası tipi (Örn: BARRACKS, STABLE, WORKSHOP)", example = "BARRACKS")
            @PathVariable String buildingType) {

        try {
            BuildingType type = BuildingType.valueOf(buildingType);
            return ResponseEntity.ok(recruitmentService.getRecruitmentQueue(villageId, type));
        } catch (IllegalArgumentException e) {
            // Frontend'e hangi tiplerin geçerli olduğunu bildiriyoruz.
            throw new RuntimeException("Geçersiz bina tipi! Lütfen BARRACKS, STABLE, WORKSHOP veya ACADEMY kullanın.");
        }
    }
}
