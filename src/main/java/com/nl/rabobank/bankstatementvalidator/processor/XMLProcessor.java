package com.nl.rabobank.bankstatementvalidator.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.nl.rabobank.bankstatementvalidator.constant.ApplicationConstant;
import com.nl.rabobank.bankstatementvalidator.domain.TransactionData;
import com.nl.rabobank.bankstatementvalidator.domain.XMLStatementRecord;
import com.nl.rabobank.bankstatementvalidator.domain.XMLStatementRecords;
import com.nl.rabobank.bankstatementvalidator.exception.TransactionDataInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class XMLProcessor implements FileProcessor {

    @Override
    public List<TransactionData> process(InputStream inputStream) {
        ObjectMapper xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new ParameterNamesModule());
        xmlMapper.registerModule(new Jdk8Module());
        xmlMapper.registerModule(new JavaTimeModule());
        XMLStatementRecords statementRecords;
        try {
            statementRecords = xmlMapper.readValue(inputStream, XMLStatementRecords.class);
        } catch (IOException | IllegalArgumentException e) {
            log.error("Exception Occured {}", e.getMessage());
            throw new RuntimeException("fail to parse XML file: " + e.getMessage());
        }
        return convert(statementRecords);
    }

    private List<TransactionData> convert(XMLStatementRecords statementRecords) {
        return statementRecords.getXmlRecords().stream().map(this::mapXmlStatementRecord).collect(Collectors.toList());
    }

    private TransactionData mapXmlStatementRecord(XMLStatementRecord xmlRecord) {
        validateXmlReference(xmlRecord);
        validateXmlAccountNumber(xmlRecord);
        validateXmlStartBalance(xmlRecord);
        validateXmlMutation(xmlRecord);
        validateXmlEndBalance(xmlRecord);
        return TransactionData.builder()
                .referenceNo(Long.parseLong(xmlRecord.getReference()))
                .accountNo(xmlRecord.getAccountNumber())
                .description(xmlRecord.getDescription())
                .startBalance(new BigDecimal(xmlRecord.getStartBalance()))
                .mutation(new BigDecimal(xmlRecord.getMutation()))
                .endBalance(new BigDecimal(xmlRecord.getEndBalance()))
                .build();
    }

    private void validateXmlEndBalance(XMLStatementRecord xmlRecord) {
        if (!StringUtils.hasText(xmlRecord.getEndBalance())) {
            throw new TransactionDataInputException(ApplicationConstant.END_BALANCE_IS_MANDATORY);
        }
    }

    private void validateXmlMutation(XMLStatementRecord xmlRecord) {
        if (!StringUtils.hasText(xmlRecord.getMutation())) {
            throw new TransactionDataInputException(ApplicationConstant.MUTATION_IS_MANDATORY);
        }
    }

    private void validateXmlStartBalance(XMLStatementRecord xmlRecord) {
        if (!StringUtils.hasText(xmlRecord.getStartBalance())) {
            throw new TransactionDataInputException(ApplicationConstant.START_BALANCE_IS_MANDATORY);
        }
    }

    private void validateXmlAccountNumber(XMLStatementRecord xmlRecord) {
        String accountNumberFromXml = xmlRecord.getAccountNumber();
        if (StringUtils.hasText(accountNumberFromXml)) {
            boolean isIBAN = Pattern.compile(ApplicationConstant.REGEX_IBAN).matcher(accountNumberFromXml).matches();
            if (!isIBAN) {
                throw new TransactionDataInputException(ApplicationConstant.ACCOUNT_NUMBER_FORMAT);
            }
        } else {
            throw new TransactionDataInputException(ApplicationConstant.ACCOUNT_NUMBER_IS_MANDATORY);
        }
    }

    private void validateXmlReference(XMLStatementRecord xmlRecord) {
        String referenceFromXml = xmlRecord.getReference();
        if (!StringUtils.hasText(referenceFromXml)) {
            throw new TransactionDataInputException(ApplicationConstant.TRANSACTION_REFERENCE_IS_MANDATORY);
        }
    }

}
