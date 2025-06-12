package com.jamaa.service_auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubAdminLoginRequest {
    private String login;
    private String password;
}