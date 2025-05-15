package com.jamaa.service_auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SuperAdminLoginRequest {
    private String login;
    private String password;
}
