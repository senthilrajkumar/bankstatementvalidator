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
class XMLProcessorTest {

    @Test
    public void processXmlDataForSuccess() {
        FileProcessor xmlProcessor = new XMLProcessor();
        List<TransactionData> records = xmlProcessor.process(getClass().getResourceAsStream("/records.xml"));

        assertEquals(10, records.size());
    }

    @Test
    public void processXmlWhenAccountNumberMissing() {
        FileProcessor xmlProcessor = new XMLProcessor();
        Exception exception = assertThrows(
                TransactionDataInputException.class,
                () -> xmlProcessor.process(getClass().getResourceAsStream("/records-input-account-number-missing.xml")));
        assertTrue(exception.getMessage().contains("Account number is mandatory"));
    }

    @Test
    public void processXmlWhenReferenceMissing() {
        FileProcessor xmlProcessor = new XMLProcessor();
        Exception exception = assertThrows(
                TransactionDataInputException.class,
                () -> xmlProcessor.process(getClass().getResourceAsStream("/records-input-missing-reference.xml")));
        assertTrue(exception.getMessage().contains("Transaction reference is mandatory"));
    }

    @Test
    public void processXmlWhenStartBalanceMissing() {
        FileProcessor xmlProcessor = new XMLProcessor();
        Exception exception = assertThrows(
                TransactionDataInputException.class,
                () -> xmlProcessor.process(getClass().getResourceAsStream("/records-input-missing-startbalance-missing.xml")));
        assertTrue(exception.getMessage().contains("Start Balance is mandatory"));
    }

    @Test
    public void processXmlWhenMutationMissing() {
        FileProcessor xmlProcessor = new XMLProcessor();
        Exception exception = assertThrows(
                TransactionDataInputException.class,
                () -> xmlProcessor.process(getClass().getResourceAsStream("/records-input-missing-mutation.xml")));
        assertTrue(exception.getMessage().contains("Mutation is mandatory"));
    }

    @Test
    public void processForWrongFormatOfXml() {
        FileProcessor xmlProcessor = new XMLProcessor();
        Exception exception = assertThrows(
                RuntimeException.class,
                () -> xmlProcessor.process(null));
        assertTrue(exception.getMessage().contains("fail to parse XML file:"));
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
