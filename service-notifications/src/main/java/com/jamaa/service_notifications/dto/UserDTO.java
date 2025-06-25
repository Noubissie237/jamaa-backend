package com.jamaa.service_notifications.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@Getter
@Setter
public class UserDTO {
    private String email;
    private String firstName;
    private String lastName;
    
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "").trim();
    }
}