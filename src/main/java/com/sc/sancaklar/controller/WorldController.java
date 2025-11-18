package com.sc.sancaklar.controller;

import com.sc.sancaklar.model.dto.JoinWorldRequest;
import com.sc.sancaklar.model.dto.WorldModel;
import com.sc.sancaklar.model.dto.village.VillageModel;
import com.sc.sancaklar.service.WorldService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/world")
@RequiredArgsConstructor
public class WorldController {
    private final WorldService worldService;

    // Giriş ekranında sadece aktif dünyalar listelenir
    @GetMapping("/active")
    public ResponseEntity<List<WorldModel>> getActiveWorlds() {
        return ResponseEntity.ok(worldService.getActiveWorlds());
    }

    // --- AŞAĞIDAKİLER GENELDE ADMIN PANELİ İÇİNDİR ---

    @PostMapping("/add")
    public ResponseEntity<WorldModel> createWorld(@RequestBody WorldModel worldModel) {
        // 201 Created kodu dönmek daha profesyoneldir
        return new ResponseEntity<>(worldService.createWorld(worldModel), HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<WorldModel>> getAllWorlds() {
        return ResponseEntity.ok(worldService.getAllWorlds());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorld(@PathVariable Long id) {
        worldService.deleteWorld(id);
        return ResponseEntity.noContent().build(); // 204 No Content (Başarılı ama veri dönmüyorum)
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<WorldModel> deactivateWorld(@PathVariable Long id) {
        return ResponseEntity.ok(worldService.deactivateWorld(id));
    }

    @PostMapping("/enter-world")
    public ResponseEntity<List<VillageModel>> enterWorld(@RequestBody JoinWorldRequest request) {
        List<VillageModel> villages = worldService.enterWorld(request);
        return ResponseEntity.ok(villages);
    }
}
