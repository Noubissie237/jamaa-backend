package com.jamaa.service_users.events;

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
    @JsonProperty("id")
    private Long id;
    @JsonProperty("firstName")
    private String firstName;
    @JsonProperty("lastName")
    private String lastName;
    @JsonProperty("email")
    private String email;
    @JsonProperty("cniRecto")
    private String cniRecto;
    @JsonProperty("cniVerso")
    private String cniVerso;
    @JsonProperty("cniNumber")
    private String cniNumber;
}
