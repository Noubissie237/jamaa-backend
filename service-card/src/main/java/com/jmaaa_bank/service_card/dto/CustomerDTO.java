package com.jmaaa_bank.service_card.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class CustomerDTO {

    @JsonProperty("customerId")
    private Long customerId;
    @JsonProperty("holderName")
    private String holderName;
    
}
