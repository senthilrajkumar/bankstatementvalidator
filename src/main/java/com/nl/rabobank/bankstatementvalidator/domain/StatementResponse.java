package com.nl.rabobank.bankstatementvalidator.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class StatementResponse{
	
	@JsonProperty("result")
	private String result;
	
	@JsonProperty("errorRecords")
	private List<ErrorRecord> errorRecords;

}
