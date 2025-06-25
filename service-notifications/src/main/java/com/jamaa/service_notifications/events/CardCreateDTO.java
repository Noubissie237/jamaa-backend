package com.jamaa.service_notifications.events;
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
