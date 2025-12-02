package com.sc.sancaklar.service;

import com.sc.sancaklar.model.dto.JoinWorldRequest;
import com.sc.sancaklar.model.dto.PlayerModel;
import com.sc.sancaklar.model.dto.WorldModel;
import com.sc.sancaklar.model.dto.village.VillageModel;
import com.sc.sancaklar.model.entity.PlayerEntity;
import com.sc.sancaklar.model.entity.UserEntity;
import com.sc.sancaklar.model.entity.WorldEntity;
import com.sc.sancaklar.model.enums.RegionDirection;
import com.sc.sancaklar.model.mapper.PlayerConverter;
import com.sc.sancaklar.model.mapper.WorldConverter;
import com.sc.sancaklar.repository.PlayerRepository;
import com.sc.sancaklar.repository.UserRepository;
import com.sc.sancaklar.repository.WorldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorldServiceImpl implements WorldService {
    private final WorldRepository worldRepository;
    private final WorldConverter worldConverter;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;
    private final VillageService villageService;
    private final PlayerConverter playerConverter;

    /**
     * Yeni bir dünya oluşturur.
     * @param worldModel Frontend'den gelen veri
     * @return Kaydedilen dünyanın Model hali
     */
    @Transactional
    public WorldModel createWorld(WorldModel worldModel) {
        // 1. Model'i Entity'ye çevir
        WorldEntity entity = worldConverter.toEntity(worldModel);

        // 2. Veritabanına kaydet
        WorldEntity savedEntity = worldRepository.save(entity);

        // 3. Kaydedilen Entity'yi tekrar Model'e çevirip dön
        return worldConverter.toModel(savedEntity);
    }

    /**
     * Sadece aktif (oynanabilir) dünyaları getirir.
     * Giriş ekranında listelemek için kullanılır.
     */
    public List<WorldModel> getActiveWorlds(Long userId) {
        // 1. Repo'dan entity listesini çek
        List<WorldEntity> activeWorlds = worldRepository.findByIsActiveTrue();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));
        List<WorldModel> worldModelList = new ArrayList<>();
        for (var world : activeWorlds) {
            WorldModel worldModel = worldConverter.toModel(world);
            Optional<PlayerEntity> existingPlayer = playerRepository.findByUserAndWorld(user, world);
            worldModel.setJoining(existingPlayer.isPresent());
            worldModelList.add(worldModel);
        }
        return worldModelList;
    }

    /**
     * Tüm dünyaları getirir (Admin paneli için gerekebilir)
     */
    public List<WorldModel> getAllWorlds() {
        return worldRepository.findAll().stream()
                .map(worldConverter::toModel)
                .toList();
    }

    /**
     * Bir dünyayı siler.
     * Dikkat: İçinde oyuncu varsa silmek risklidir, genelde soft-delete (isActive=false) yapılır.
     * Ama isteğin üzerine direkt silme yazıyorum.
     */
    @Transactional
    public void deleteWorld(Long worldId) {
        // Var mı diye kontrol et, yoksa hata fırlat
        if (!worldRepository.existsById(worldId)) {
            throw new RuntimeException("Dünya bulunamadı! ID: " + worldId);
        }

        worldRepository.deleteById(worldId);
    }

    /**
     * Bir dünyayı Pasif'e çeker (Silmek yerine kapatmak daha güvenlidir)
     */
    @Transactional
    public WorldModel deactivateWorld(Long worldId) {
        WorldEntity world = worldRepository.findById(worldId)
                .orElseThrow(() -> new RuntimeException("Dünya bulunamadı!"));

        world.setActive(false);
        WorldEntity saved = worldRepository.save(world);

        return worldConverter.toModel(saved);
    }

    @Transactional
    public List<VillageModel> enterWorld(JoinWorldRequest request) {

        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));
        WorldEntity world = worldRepository.findById(request.getWorldId())
                .orElseThrow(() -> new RuntimeException("Dünya bulunamadı!"));

        Optional<PlayerEntity> existingPlayer = playerRepository.findByUserAndWorld(user, world);

        if (existingPlayer.isPresent()) {
            // Zaten varsa bölge seçimi önemsizdir, mevcut köyleri dön
            return villageService.getVillagesByPlayerId(existingPlayer.get().getId());
        } else {
            // Yeni oyuncu oluştur
            PlayerEntity newPlayer = new PlayerEntity();
            newPlayer.setUser(user);
            newPlayer.setWorld(world);
            newPlayer.setPoints(0);
            newPlayer = playerRepository.save(newPlayer);

            PlayerModel playerModel = playerConverter.toModel(newPlayer);

            RegionDirection direction = request.getDirection() != null ? request.getDirection() : RegionDirection.RANDOM;

            VillageModel firstVillage = villageService.createFirstVillage(playerModel, direction);

            return Collections.singletonList(firstVillage);
        }
    }

}
