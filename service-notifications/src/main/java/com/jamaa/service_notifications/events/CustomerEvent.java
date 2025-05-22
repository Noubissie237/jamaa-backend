package com.jamaa.service_notifications.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Setter
@NoArgsConstructor
@ToString
public class CustomerEvent {
    @JsonProperty("firstName")
    private String firstName;
    @JsonProperty("lastName")
    private String lastName;
    @JsonProperty("email")
    private String email;
    @JsonProperty("accountNumber")
    private String accountNumber;
    @JsonProperty("error_message")
    private String errorMessage;
    @JsonProperty("deletion_reason")
    private String deletionReason;
}
