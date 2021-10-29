package com.nl.rabobank.bankstatementvalidator.domain;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class XMLStatementRecord {

    @JacksonXmlProperty(isAttribute = true)
    private String reference;

    @JacksonXmlProperty
    private String accountNumber;

    @JacksonXmlProperty
    private String description;

    @JacksonXmlProperty
    private String startBalance;

    @JacksonXmlProperty
    private String mutation;

    @JacksonXmlProperty
    private String endBalance;

}
