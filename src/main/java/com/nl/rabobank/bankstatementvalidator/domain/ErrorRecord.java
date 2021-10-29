package com.nl.rabobank.bankstatementvalidator.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ErrorRecord {

    @JsonProperty("reference")
    private String referenceNo;

    @JsonProperty("accountNumber")
    private String accountNumber;

}
