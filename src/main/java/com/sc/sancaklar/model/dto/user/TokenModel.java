package com.sc.sancaklar.model.dto.user;

import lombok.Data;

@Data
public class TokenModel {
    private String token;
    private UserModel user;
}
