package com.jamaa.banks.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerDTO {
    @JsonProperty("customerId")
    private Long customerId;
    @JsonProperty("holderName")
    private String holderName;
    @JsonProperty("bankId")
    private Long bankId;
    @JsonProperty("bankName")
    private String bankName;
}
