package com.jamaa.service_banks_account.event;



import com.fasterxml.jackson.annotation.JsonProperty;
import com.jamaa.service_banks_account.dto.BankInfoDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BankCreatedEvent {
    @JsonProperty("bankInfo")
    private BankInfoDTO bankInfo;
}
