package com.nl.rabobank.bankstatementvalidator.domain;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@JacksonXmlRootElement(localName = "records")
public class XMLStatementRecords {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "record")
    private List<XMLStatementRecord> xmlRecords;

}
