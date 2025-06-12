package com.jamaa.banks.event;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.jamaa.banks.dto.BankInfoDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BankCreatedEvent {
    @JsonProperty("bankInfo")
    private BankInfoDTO bankInfo;
}