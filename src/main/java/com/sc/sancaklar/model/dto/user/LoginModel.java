package com.sc.sancaklar.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginModel {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
