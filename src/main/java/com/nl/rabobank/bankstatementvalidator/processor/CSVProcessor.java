package com.nl.rabobank.bankstatementvalidator.processor;

import com.nl.rabobank.bankstatementvalidator.constant.ApplicationConstant;
import com.nl.rabobank.bankstatementvalidator.domain.CSVStatementRecord;
import com.nl.rabobank.bankstatementvalidator.domain.TransactionData;
import com.nl.rabobank.bankstatementvalidator.exception.TransactionDataInputException;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CSVProcessor implements FileProcessor {

    @Override
    public List<TransactionData> process(InputStream inputStream) {
        List<CSVStatementRecord> csvStatementRecords;
        try {
            csvStatementRecords = new CsvToBeanBuilder<CSVStatementRecord>(
                    new BufferedReader(new InputStreamReader(inputStream))).withOrderedResults(false)
                    .withType(CSVStatementRecord.class).build().parse();
        } catch (Exception e) {
            log.error("Exception Occured {}", e.getMessage());
            throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
        }
        return convert(csvStatementRecords);
    }

    private List<TransactionData> convert(List<CSVStatementRecord> csvStatementRecords) {
        return csvStatementRecords.stream().map(this::mapCsvStatementRecord).collect(Collectors.toList());
    }

    private TransactionData mapCsvStatementRecord(CSVStatementRecord record) {
        validateReference(record);
        validateAccountNumber(record);
        validateStartBalance(record);
        validateMutation(record);
        validateEndBalance(record);
        return TransactionData.builder()
                .referenceNo(Long.parseLong(record.getReference()))
                .accountNo(record.getAccountNumber())
                .description(record.getDescription())
                .startBalance(new BigDecimal(record.getStartBalance()))
                .mutation(new BigDecimal(record.getMutation()))
                .endBalance(new BigDecimal(record.getEndBalance()))
                .build();
    }

    private void validateEndBalance(CSVStatementRecord record) {
        String endBalance = record.getEndBalance();
        if (!StringUtils.hasText(endBalance)) {
            throw new TransactionDataInputException(ApplicationConstant.END_BALANCE_IS_MANDATORY);
        }
    }

    private void validateMutation(CSVStatementRecord record) {
        String mutation = record.getMutation();
        if (!StringUtils.hasText(mutation)) {
            throw new TransactionDataInputException(ApplicationConstant.MUTATION_IS_MANDATORY);
        }
    }

    private void validateStartBalance(CSVStatementRecord record) {
        String startBalance = record.getStartBalance();
        if (!StringUtils.hasText(startBalance)) {
            throw new TransactionDataInputException(ApplicationConstant.START_BALANCE_IS_MANDATORY);
        }
    }

    private void validateAccountNumber(CSVStatementRecord record) {
        String accountNo = record.getAccountNumber();
        if (StringUtils.hasText(accountNo)) {
            boolean isIBAN = Pattern.compile(ApplicationConstant.REGEX_IBAN).matcher(accountNo).matches();
            if (!isIBAN) {
                throw new TransactionDataInputException(ApplicationConstant.ACCOUNT_NUMBER_FORMAT);
            }
        } else {
            throw new TransactionDataInputException(ApplicationConstant.ACCOUNT_NUMBER_IS_MANDATORY);
        }
    }

    private void validateReference(CSVStatementRecord record) {
        if (!StringUtils.hasText(record.getReference())) {
            throw new TransactionDataInputException(ApplicationConstant.TRANSACTION_REFERENCE_IS_MANDATORY);
        }
    }

}
