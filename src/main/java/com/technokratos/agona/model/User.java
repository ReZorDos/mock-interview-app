package com.technokratos.agona.model;

import com.technokratos.agona.enums.Roles;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User extends AbstractEntity {

    @Column(name = "name")
    private String name;

    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Roles role;

}
