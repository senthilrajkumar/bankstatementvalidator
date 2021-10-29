package com.nl.rabobank.bankstatementvalidator.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class StatementResponse {

    @JsonProperty("result")
    private String result;

    @JsonProperty("errorRecords")
    private List<ErrorRecord> errorRecords;

}
