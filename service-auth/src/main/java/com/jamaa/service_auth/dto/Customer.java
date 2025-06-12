package com.jamaa.service_auth.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Customer {
    private Long id;
    private String email;
    private String phone;
    private String firstName;
    private String LastName;
}
