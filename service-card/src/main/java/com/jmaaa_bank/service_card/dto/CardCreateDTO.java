package com.jmaaa_bank.service_card.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class CardCreateDTO {
    @JsonProperty("email")
    private String email;
    @JsonProperty("name")
    private String name;
    @JsonProperty("cardNumber")
    private String cardNumber;
    @JsonProperty("bankName")
    private String bankName;
}
