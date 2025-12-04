package com.sc.sancaklar.controller;

import com.sc.sancaklar.model.dto.ConstructionModel;
import com.sc.sancaklar.model.dto.UpgradeBuildingRequest;
import com.sc.sancaklar.model.dto.VillageMapModel;
import com.sc.sancaklar.model.dto.village.VillageModel;
import com.sc.sancaklar.service.VillageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/village")
@RequiredArgsConstructor
@Tag(name = "Köy Yönetimi ve İnşaat", description = "Köy detaylarını görüntüleme ve binaları geliştirme işlemleri.")
public class VillageController {
    private final VillageService villageService;

    // GET: Köy detaylarını getir (Kaynakları güncelleyerek)
    @Operation(
            summary = "Köy Detaylarını Getir",
            description = "Belirtilen köyün tüm güncel detaylarını (kaynaklar, binalar, teknolojiler) Lazy Update yöntemiyle hesaplayıp getirir."
    )
    @GetMapping("/{villageId}")
    public ResponseEntity<VillageModel> getVillage(
            @Parameter(description = "Detayları istenen köyün ID'si", example = "1")
            @PathVariable Long villageId) {

        VillageModel village = villageService.getVillageById(villageId);
        return ResponseEntity.ok(village);
    }

    @Operation(
            summary = "Bina Yükseltme Emri Ver",
            description = "İstenen binanın (Örn: BARRACKS) bir sonraki seviyeye çıkarılması için inşaat emri oluşturur. Kaynak kontrolü ve düşümü bu aşamada yapılır."
    )
    @PostMapping("/build")
    public ResponseEntity<ConstructionModel> upgradeBuilding(@RequestBody UpgradeBuildingRequest request) {
        return ResponseEntity.ok(villageService.upgradeBuilding(request));
    }

    @Operation(
            summary = "İnşaat Kuyruğunu Görüntüle",
            description = "Köydeki devam eden tüm inşaat emirlerini (hangi bina, ne zaman bitecek) listeler. Frontend'deki geri sayım sayaçları bu veriyi kullanır."
    )
    @GetMapping("/constructions/{villageId}")
    public ResponseEntity<List<ConstructionModel>> getConstructions(
            @Parameter(description = "Kuyruğu istenen köyün ID'si", example = "1")
            @PathVariable Long villageId) {

        List<ConstructionModel> queue = villageService.getConstructionQueue(villageId);
        return ResponseEntity.ok(queue);
    }

    @Operation(
            summary = "Dünyadaki Tüm Köyleri Görüntüle",
            description = "Dünyadaki Tüm Köyleri listeler."
    )
    @GetMapping("/getListByWorldId/{worldId}")
    public ResponseEntity<List<VillageMapModel>> getListByWorldId(
            @Parameter(description = "İlgili Dünya ID'si", example = "1")
            @PathVariable Long worldId) {

        List<VillageMapModel> villageModelList = villageService.getListByWorldId(worldId);
        return ResponseEntity.ok(villageModelList);
    }

    @Operation(
            summary = "Kuyruktaki inşaat silme",
            description = "Kuyruktaki inşaatı siler."
    )
    @DeleteMapping("/cancelConstruction/{constructionId}")
    public void cancelConstruction(
            @Parameter(description = "Kuyruk ID", example = "1")
            @PathVariable Long constructionId) {
        villageService.cancelConstruction(constructionId);
    }

    @PutMapping("update")
    public void update(@RequestBody VillageModel villageModel) {
        villageService.update(villageModel);
    }
}
