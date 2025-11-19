package com.sc.sancaklar.controller;

import com.sc.sancaklar.model.dto.ResponseMessageModel;
import com.sc.sancaklar.model.dto.user.*;
import com.sc.sancaklar.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Kullanıcı ve Yetkilendirme (AUTH)", description = "Sisteme giriş, kayıt, şifre ve kullanıcı bilgileri işlemleri.") // Class seviyesi tag
public class AuthController {
    @Autowired
    private AuthService authService;

    @Operation(
            summary = "Kullanıcı Bilgilerini ID ile Getir",
            description = "Belirtilen ID'ye sahip kullanıcının temel bilgilerini getirir."
    )
    @GetMapping("/getById/{id}")
    public ResponseEntity<UserModel> getById(
            @Parameter(description = "Bilgileri istenen kullanıcının ID'si", example = "1")
            @PathVariable(name = "id") @NotNull Long id) {

        return ResponseEntity.ok(authService.getById(id));
    }

    @Operation(
            summary = "Kullanıcı Kaydı (Register)",
            description = "Yeni bir kullanıcı hesabı oluşturur."
    )
    @PostMapping("/register")
    public ResponseEntity<UserModel> register(@RequestBody UserModel userModel) {
        return ResponseEntity.ok(authService.register(userModel));
    }

    @Operation(
            summary = "Kullanıcı Bilgilerini Güncelle",
            description = "Mevcut kullanıcının (ID ile belirlenen) temel bilgilerini günceller."
    )
    @PutMapping("/update")
    public ResponseEntity<UserModel> updateUser(@RequestBody UserModel userModel) {
        return ResponseEntity.ok(authService.updateUser(userModel));
    }

    @Operation(
            summary = "Sisteme Giriş (Login)",
            description = "Kullanıcı adı/e-posta ve şifre ile giriş yaparak JWT/Token elde eder."
    )
    @PostMapping("/login")
    public ResponseEntity<TokenModel> login(@RequestBody LoginModel loginModel) {
        return ResponseEntity.ok(authService.login(loginModel));
    }

    @Operation(
            summary = "Şifre Yenileme Servisi",
            description = "Kullanıcı şifresini unuttuğunda mail adresine yeni şifre/reset linki göndermeye yarar."
    )
    @PostMapping("/refreshPassword")
    public ResponseEntity<ResponseMessageModel> refreshPassword(@RequestBody PasswordRefreshModel passwordRefreshModel) {
        return ResponseEntity.ok(authService.refreshPassword(passwordRefreshModel));
    }
}
