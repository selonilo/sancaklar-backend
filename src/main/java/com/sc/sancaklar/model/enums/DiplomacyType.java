package com.sc.sancaklar.model.enums;

import lombok.Getter;

@Getter
public enum DiplomacyType {
    ALLY("Müttefik", "#0000FF"),            // Mavi (Dost)
    NAP("Saldırmazlık Anlaşması", "#800080"), // Mor (Nötr/Barış)
    ENEMY("Düşman", "#FF0000");             // Kırmızı (Savaş)

    private final String displayName;
    private final String hexColor;

    DiplomacyType(String displayName, String hexColor) {
        this.displayName = displayName;
        this.hexColor = hexColor;
    }
}
