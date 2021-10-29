package com.nl.rabobank.bankstatementvalidator.processor;

import com.nl.rabobank.bankstatementvalidator.domain.TransactionData;
import com.nl.rabobank.bankstatementvalidator.exception.TransactionDataInputException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class CSVProcessorTest {

    @Test
    public void processSuccess() {
        FileProcessor csvProcessor = new CSVProcessor();
        List<TransactionData> records = csvProcessor.process(getClass().getResourceAsStream("/records.csv"));

        assertEquals(10, records.size());
    }

    @Test
    public void processWhenAccountNumberMissing() {
        FileProcessor csvProcessor = new CSVProcessor();
        Exception exception = assertThrows(
                TransactionDataInputException.class,
                () -> csvProcessor.process(getClass().getResourceAsStream("/records-input-missing-accountnumber.csv")));
        assertTrue(exception.getMessage().contains("Account number is mandatory"));
    }

    @Test
    public void processWhenReferenceMissing() {
        FileProcessor csvProcessor = new CSVProcessor();
        Exception exception = assertThrows(
                TransactionDataInputException.class,
                () -> csvProcessor.process(getClass().getResourceAsStream("/records-input-missing-reference.csv")));
        assertTrue(exception.getMessage().contains("Transaction reference is mandatory"));
    }

    @Test
    public void processWhenStartBalanceMissing() {
        FileProcessor csvProcessor = new CSVProcessor();
        Exception exception = assertThrows(
                TransactionDataInputException.class,
                () -> csvProcessor.process(getClass().getResourceAsStream("/records-input-missing-startbalance.csv")));
        assertTrue(exception.getMessage().contains("Start Balance is mandatory"));
    }

    @Test
    public void processWhenMutationMissing() {
        FileProcessor csvProcessor = new CSVProcessor();
        Exception exception = assertThrows(
                TransactionDataInputException.class,
                () -> csvProcessor.process(getClass().getResourceAsStream("/records-input-missing-mutation.csv")));
        assertTrue(exception.getMessage().contains("Mutation is mandatory"));
    }

    @Test
    public void processWhenEndBalanceMissing() {
        FileProcessor csvProcessor = new CSVProcessor();
        Exception exception = assertThrows(
                TransactionDataInputException.class,
                () -> csvProcessor.process(getClass().getResourceAsStream("/records-input-missing-endbalance.csv")));
        assertTrue(exception.getMessage().contains("End Balance is mandatory"));
    }

    @Test
    public void processForWrongFormatAccountNumber() {
        FileProcessor csvProcessor = new CSVProcessor();
        Exception exception = assertThrows(
                TransactionDataInputException.class,
                () -> csvProcessor.process(getClass().getResourceAsStream("/records-improper-account-number.csv")));
        assertTrue(exception.getMessage().contains("Account Number should be in the right format"));
    }

    @Test
    public void processForWrongFormatOfCsv() {
        FileProcessor csvProcessor = new CSVProcessor();
        Exception exception = assertThrows(
                RuntimeException.class,
                () -> csvProcessor.process(null));
        assertTrue(exception.getMessage().contains("fail to parse CSV file"));
    }

}
