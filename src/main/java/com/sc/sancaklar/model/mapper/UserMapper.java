package com.sc.sancaklar.model.mapper;

import com.sc.sancaklar.model.dto.user.UserModel;
import com.sc.sancaklar.model.entity.UserEntity;

import java.util.List;

public class UserMapper {

    public static UserModel mapTo(UserEntity entity) {
        UserModel model = new UserModel();
        model.setId(entity.getId());
        model.setUpdatedDate(entity.getUpdatedDate());
        model.setCreatedDate(entity.getCreatedDate());
        model.setUpdatedBy(entity.getUpdatedBy());
        model.setCreatedBy(entity.getCreatedBy());
        model.setUsername(entity.getUsername());
        model.setEmail(entity.getEmail());
        model.setPassword(entity.getPassword());
        return model;
    }

    public static UserEntity mapTo(UserModel model) {
        UserEntity entity = new UserEntity();
        entity.setId(model.getId());
        entity.setUpdatedDate(model.getUpdatedDate());
        entity.setCreatedDate(model.getCreatedDate());
        entity.setUpdatedBy(model.getUpdatedBy());
        entity.setCreatedBy(model.getCreatedBy());
        entity.setUsername(model.getUsername());
        entity.setEmail(model.getEmail());
        entity.setPassword(model.getPassword());
        return entity;
    }

    public static List<UserModel> mapToList(List<UserEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(UserMapper::mapTo).toList();
    }
}
