package com.sc.sancaklar.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordRefreshModel {
    @NotBlank
    private String mail;
}
