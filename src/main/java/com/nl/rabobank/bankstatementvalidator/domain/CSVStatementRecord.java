package com.nl.rabobank.bankstatementvalidator.domain;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;

@Data
public class CSVStatementRecord {
	

    @CsvBindByName
    private String reference;

    @CsvBindByName(column = "Account Number")
    private String accountNumber;

    @CsvBindByName
    private String description;

    @CsvBindByName(column = "Start Balance")
    private String startBalance;

    @CsvBindByName
    private String mutation;

    @CsvBindByName(column = "End Balance")
    private String endBalance;

}
