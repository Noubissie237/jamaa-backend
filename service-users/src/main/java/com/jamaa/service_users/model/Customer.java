package com.jamaa.service_users.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Entity
@Getter
@Setter
public class Customer extends Person {
    @Column(nullable = true, unique = true)
    private String phone;
    @Column(nullable = false)
    private String cniNumber;
    @Column(nullable = false)
    private String cniRecto;
    @Column(nullable = false)
    private String cniVerso;
    private Boolean isVerified;
}
