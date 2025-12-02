package com.sc.sancaklar.controller;

import com.sc.sancaklar.model.dto.JoinWorldRequest;
import com.sc.sancaklar.model.dto.WorldModel;
import com.sc.sancaklar.model.dto.village.VillageModel;
import com.sc.sancaklar.service.WorldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/world")
@RequiredArgsConstructor
@Tag(name = "Dünya Yönetimi ve Giriş", description = "Oyun dünyalarını yönetme (Admin) ve oyuncu girişi yapma işlemlerini içerir.")
public class WorldController {
    private final WorldService worldService;

    // --- GENEL ERİŞİM ---

    @Operation(
            summary = "Aktif Dünyaları Listele",
            description = "Kullanıcıların **giriş yapabileceği** aktif ve açık durumdaki tüm dünyaları listeler."
    )
    @GetMapping("/active/{userId}")
    public ResponseEntity<List<WorldModel>> getActiveWorlds(@PathVariable(value = "userId") Long userId) {
        return ResponseEntity.ok(worldService.getActiveWorlds(userId));
    }

    // --- KRİTİK GİRİŞ NOKTASI ---

    @Operation(
            summary = "Dünyaya Giriş Yap / İlk Köyü Kur",
            description = "Kullanıcının belirtilen dünyaya giriş yapmasını sağlar. Eğer oyuncunun bu dünyada **kaydı varsa** mevcut köylerini listeler; **yoksa** yeni bir Player oluşturur ve ilk köyünü kurar."
    )
    @PostMapping("/enter-world")
    public ResponseEntity<List<VillageModel>> enterWorld(@RequestBody JoinWorldRequest request) {
        List<VillageModel> villages = worldService.enterWorld(request);
        return ResponseEntity.ok(villages);
    }

    // --- YÖNETİM (ADMIN) PANELİ İÇİN ---

    @Operation(
            summary = "Yeni Dünya Oluştur (Admin)",
            description = "Yeni bir oyun dünyası (World) oluşturur ve varsayılan ayarları (hız, aktiflik) yapar."
    )
    @PostMapping("/add")
    public ResponseEntity<WorldModel> createWorld(@RequestBody WorldModel worldModel) {
        return new ResponseEntity<>(worldService.createWorld(worldModel), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Tüm Dünyaları Getir (Admin)",
            description = "Tüm **aktif ve pasif** durumdaki dünyaları listeler. Yönetim paneli için kullanılır."
    )
    @GetMapping("/all")
    public ResponseEntity<List<WorldModel>> getAllWorlds() {
        return ResponseEntity.ok(worldService.getAllWorlds());
    }

    @Operation(
            summary = "Dünyayı Sil (Admin)",
            description = "Belirtilen ID'ye sahip dünyayı **kalıcı olarak siler**. Tüm bağlı oyuncu verileri silinecektir."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorld(
            @Parameter(description = "Silinecek dünyanın ID'si", example = "2")
            @PathVariable Long id) {

        worldService.deleteWorld(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Dünyayı Pasifleştir (Admin)",
            description = "Bir dünyanın **aktiflik durumunu 'pasif' yapar**. Yeni oyuncu girişi durdurulur."
    )
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<WorldModel> deactivateWorld(
            @Parameter(description = "Pasifleştirilecek dünyanın ID'si", example = "1")
            @PathVariable Long id) {

        return ResponseEntity.ok(worldService.deactivateWorld(id));
    }
}
