package com.jamaa.service_account.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class CustomerDTO {

    @JsonProperty("customerId")
    private Long customerId;
    @JsonProperty("email")
    private String email;
}