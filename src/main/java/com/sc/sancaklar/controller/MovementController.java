package com.sc.sancaklar.controller;

import com.sc.sancaklar.model.dto.MovementTrackerModel;
import com.sc.sancaklar.model.dto.SendTroopsRequest;
import com.sc.sancaklar.service.MovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movement")
@RequiredArgsConstructor
@Tag(name = "Askeri Hareketler ve Komuta", description = "Ordu gönderme, takip etme ve komuta işlemlerini içerir.")
public class MovementController {

    private final MovementService movementService;

    @Operation(
            summary = "Askeri Emir Gönder (Saldırı/Destek)",
            description = "Kaynak köyden hedef köye asker gönderir. Köy stoğundan düşüm, mesafe/süre hesaplaması ve varış emri oluşturulur. Tip (ATTACK, SUPPORT) belirtilmelidir."
    )
    @PostMapping("/send")
    public ResponseEntity<String> sendTroops(@RequestBody SendTroopsRequest request) {
        try {
            // Güvenlik katmanında request'in sourceVillageId'sinin oyuncuya ait olduğu doğrulanmalıdır!
            movementService.sendTroops(request);
            return ResponseEntity.ok("Ordu başarıyla yola çıktı. Hareket Tipi: " + request.getType());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Ordu gönderilemedi: " + e.getMessage());
        }
    }

    /**
     * GET /api/v1/movements/outgoing/{playerId}
     * Oyuncunun aktif olan tüm giden askeri hareketlerini listeler.
     */
    @Operation(
            summary = "Giden Orduları Takip Et",
            description = "Belirtilen oyuncu tarafından gönderilmiş, hala yolda olan (varış saati gelmemiş) tüm askeri hareketleri listeler. Geri sayım ve harita takibi için kullanılır."
    )
    @GetMapping("/outgoing/{playerId}")
    public ResponseEntity<List<MovementTrackerModel>> getOutgoingMovements(
            @Parameter(description = "Takip edilecek oyuncunun ID'si", example = "1")
            @PathVariable Long playerId) {

        List<MovementTrackerModel> movements = movementService.getOutgoingMovementsByPlayer(playerId);
        return ResponseEntity.ok(movements);
    }
}
