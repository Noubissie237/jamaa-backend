package com.jamaa.service_users.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class SubAdmin extends Person {
    @Column(nullable = false, unique = true)
    private String username;
}
