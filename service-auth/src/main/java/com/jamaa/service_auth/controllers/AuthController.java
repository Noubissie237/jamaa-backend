package com.jamaa.service_auth.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jamaa.service_auth.dto.CustomerLoginRequest;
import com.jamaa.service_auth.services.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    AuthService authService;

    @PostMapping("/login")
    public String login(@RequestBody CustomerLoginRequest loginRequest) throws Exception {
        return authService.customerLogin(loginRequest.getLogin(), loginRequest.getPassword());
    }

    // @PostMapping("/check-password")
    // public boolean checkPassword(@RequestBody CustomerLoginRequest checking) {
    //     return authService.checkPassword(checking.getPhone(), checking.getPassword());
    // }
    
    // @PostMapping("/login-admin")
    // public ResponseEntity<?>  loginAdmin(@RequestBody SubAdminLoginRequest admin) {
    //     System.out.println("recu");
    //     return authService.loginAdmin(admin.getLogin(), admin.getPassword());
    // }
    
}
