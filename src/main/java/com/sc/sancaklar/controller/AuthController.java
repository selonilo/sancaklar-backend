package com.sc.sancaklar.controller;

import com.sc.sancaklar.model.dto.ResponseMessageModel;
import com.sc.sancaklar.model.dto.user.*;
import com.sc.sancaklar.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @GetMapping("/getById/{id}")
    public ResponseEntity<UserModel> getById(@PathVariable(name = "id") @NotNull Long id) {
        return ResponseEntity.ok(authService.getById(id));
    }

    @PostMapping("/register")
    public ResponseEntity<UserModel> register(@RequestBody UserModel userModel) {
        return ResponseEntity.ok(authService.register(userModel));
    }

    @PutMapping("/update")
    public ResponseEntity<UserModel> updateUser(@RequestBody UserModel userModel) {
        return ResponseEntity.ok(authService.updateUser(userModel));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenModel> login(@RequestBody LoginModel loginModel) {
        return ResponseEntity.ok(authService.login(loginModel));
    }

    @Operation(summary = "Şifre yenileme servisi", description = "Kullanıcı şifresini unuttuğunda mail adresine şifre göndermeye yarar.")
    @PostMapping("/refreshPassword")
    public ResponseEntity<ResponseMessageModel> refreshPassword(@RequestBody PasswordRefreshModel passwordRefreshModel) {
        return ResponseEntity.ok(authService.refreshPassword(passwordRefreshModel));
    }
}
