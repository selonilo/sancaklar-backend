package com.sc.sancaklar.model.entity;

import com.sc.sancaklar.model.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USERS")
public class UserEntity extends BaseEntity {
    @NotBlank
    @Column(name = "USERNAME", unique = true, nullable = false, length = 60)
    private String username;

    @NotBlank
    @Column(name = "EMAIL", length = 60, unique = true)
    private String email;

    @NotBlank
    @Column(name = "PASSWORD", length = 60)
    private String password;
}
