package com.sc.sancaklar.model.dto;

import com.sc.sancaklar.model.enums.AllianceRole;
import lombok.Data;

@Data
public class PlayerModel {
    private Long id;
    private String username;    // UserEntity'den gelecek
    private String worldName;   // WorldEntity'den gelecek
    private int points;

    // Klan Bilgileri (Eğer klanı yoksa null olabilir)
    private Long allianceId;
    private String allianceName;
    private String allianceTag;
    private AllianceRole allianceRole; // Klandaki rütbesi (Kurucu, Üye vs.)

    private int rank; // Dünya sıralaması (Bunu veritabanında tutmasak bile serviste hesaplayıp buraya koyabiliriz)
}