package com.sc.sancaklar.controller;

import com.sc.sancaklar.model.dto.ActiveCountModel;
import com.sc.sancaklar.model.dto.ResearchRequest;
import com.sc.sancaklar.service.ResearchService;
import com.sc.sancaklar.service.StatisticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistic")
@RequiredArgsConstructor
@Tag(name = "Anasayfa && İstatistik", description = "Ana sayfadaki istatistik bilgileri.")
public class StatisticController {
    private final StatisticService statisticService;

    @Operation(
            summary = "Anasayfa info",
            description = "Ana sayfada toplam oyuncu, köy ve savaş sayılarını döner."
    )
    @GetMapping("/activeCount")
    public ResponseEntity<ActiveCountModel> getActiveCount() {
        return ResponseEntity.ok(statisticService.getActiveCount());
    }
}
