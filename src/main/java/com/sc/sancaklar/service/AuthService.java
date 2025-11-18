package com.sc.sancaklar.service;

import com.sc.sancaklar.model.dto.ResponseMessageModel;
import com.sc.sancaklar.model.dto.user.*;

public interface AuthService {
    UserModel getById(Long id);
    UserModel register(UserModel userModel);
    UserModel updateUser(UserModel userModel);
    TokenModel login(LoginModel loginModel);
    ResponseMessageModel refreshPassword(PasswordRefreshModel passwordRefreshModel);
}
