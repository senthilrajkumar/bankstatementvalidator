package com.nl.rabobank.bankstatementvalidator.service;

import com.nl.rabobank.bankstatementvalidator.dao.BankStatementDao;
import com.nl.rabobank.bankstatementvalidator.domain.StatementResponse;
import com.nl.rabobank.bankstatementvalidator.domain.TransactionData;
import com.nl.rabobank.bankstatementvalidator.exception.BankStatementDBException;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
class BankStatementServiceImplTest {

    @Autowired
    private BankStatementService bankStatementService;

    @Mock
    MultipartFile multipartFile;

    @MockBean
    private BankStatementDao bankStatementDao;

    private StatementResponse response;

    @BeforeEach
    void setUp() {
        response = new StatementResponse();
    }

    @AfterEach
    void tearDown() {
        response = null;
    }

    @Before(value = "")
    public void init() {
        when(multipartFile.getContentType()).thenReturn("application/vnd.ms-excel");
    }

    @Test
    void testProcessMethodForSuccess() {
        TransactionData data = TransactionData.builder()
                .referenceNo(1L)
                .accountNo("NL91 ABNA 0417 1643 01")
                .startBalance(new BigDecimal("2.0"))
                .endBalance(new BigDecimal("4.0"))
                .mutation(new BigDecimal("+2.0")).build();
        TransactionData dataTwo = TransactionData.builder()
                .referenceNo(2L)
                .accountNo("NL91 BANA 0417 1643 02")
                .startBalance(new BigDecimal("1.0"))
                .endBalance(new BigDecimal("2.0"))
                .mutation(new BigDecimal("+1.0")).build();
        List<TransactionData> transactionData = new ArrayList<>();
        transactionData.add(data);
        transactionData.add(dataTwo);
        when(bankStatementDao.checkTransactionRecordExists(Mockito.anyLong())).thenReturn(false);
        response = bankStatementService.processTransactionRecords(transactionData);
        assertEquals("SUCCESSFUL", response.getResult());
        assertEquals(0, response.getErrorRecords().size());
    }

    @Test
    void testProcessMethodForDuplicateReference() {
        TransactionData data = TransactionData.builder()
                .referenceNo(1L)
                .accountNo("NL91 ABNA 0417 1643 01")
                .startBalance(new BigDecimal("2.0"))
                .endBalance(new BigDecimal("4.0"))
                .mutation(new BigDecimal("+2.0"))
                .build();
        TransactionData dataTwo = TransactionData.builder()
                .referenceNo(2L)
                .accountNo("NL91 BANA 0417 1643 02")
                .startBalance(new BigDecimal("1.0"))
                .endBalance(new BigDecimal("2.0"))
                .mutation(new BigDecimal("+1.0")).build();
        List<TransactionData> transactionData = new ArrayList<>();
        transactionData.add(data);
        transactionData.add(dataTwo);
        when(bankStatementDao.checkTransactionRecordExists(1L)).thenReturn(true);
        response = bankStatementService.processTransactionRecords(transactionData);
        assertEquals("DUPLICATE_REFERENCE", response.getResult());
        assertEquals(1, response.getErrorRecords().size());
    }

    @Test
    void testProcessMethodForInCorrectEndBalance() {
        TransactionData data = TransactionData.builder()
                .referenceNo(4L)
                .accountNo("NL91 ABNA 0417 1643 01")
                .startBalance(new BigDecimal("4.0"))
                .endBalance(new BigDecimal("1.0"))
                .mutation(new BigDecimal("-2.0")).build();
        List<TransactionData> transactionData = new ArrayList<>();
        transactionData.add(data);
        when(bankStatementDao.checkTransactionRecordExists(4L)).thenReturn(false);
        response = bankStatementService.processTransactionRecords(transactionData);
        assertEquals("INCORRECT_END_BALANCE", response.getResult());
        assertEquals(1, response.getErrorRecords().size());
    }

    @Test
    void testProcessMethodForBothDuplicateAndInCorrectBalance() {
        TransactionData data = TransactionData.builder()
                .referenceNo(5L)
                .accountNo("NL91 ABNA 0417 1643 01")
                .startBalance(new BigDecimal("4.0"))
                .endBalance(new BigDecimal("1.0"))
                .mutation(new BigDecimal("-1.0")).build();
        List<TransactionData> transactionData = new ArrayList<>();
        transactionData.add(data);
        when(bankStatementDao.checkTransactionRecordExists(5L)).thenReturn(true);
        response = bankStatementService.processTransactionRecords(transactionData);
        assertEquals("DUPLICATE_REFERENCE_INCORRECT_END_BALANCE", response.getResult());
        assertEquals(2, response.getErrorRecords().size());
    }

    @Test
    void testProcessMethodForInternalServerError() {
        TransactionData data = TransactionData.builder()
                .referenceNo(6L)
                .accountNo("NL91 ABNA 0417 1643 15")
                .startBalance(new BigDecimal("4.0"))
                .endBalance(new BigDecimal("2.0"))
                .mutation(new BigDecimal("-2.0")).build();
        List<TransactionData> transactionData = new ArrayList<>();
        transactionData.add(data);
        when(bankStatementDao.checkTransactionRecordExists(6L)).thenThrow(new RuntimeException("DB is not available"));
        assertThrows(BankStatementDBException.class, () -> bankStatementService.processTransactionRecords(transactionData));
    }

    //this test case covers the input json itself contains duplicates and also have duplicates when invoke db and Incorrect balance
    @Test
    void testWhenBothDuplicateAndInCorrectBalanceForMultipleRec() {
        TransactionData data = TransactionData.builder()
                .referenceNo(20L)
                .accountNo("NL91 ABNA 0417 1643 20")
                .startBalance(new BigDecimal("4.0"))
                .endBalance(new BigDecimal("1.0"))
                .mutation(new BigDecimal("-2.0")).build();
        TransactionData dataTwo = TransactionData.builder()
                .referenceNo(21L)
                .accountNo("NL91 ABNA 0417 1643 21")
                .startBalance(new BigDecimal("4.0"))
                .endBalance(new BigDecimal("5.0"))
                .mutation(new BigDecimal("+1.0")).build();
        TransactionData dataThree = TransactionData.builder()
                .referenceNo(22L)
                .accountNo("NL91 ABNA 0417 1643 22")
                .startBalance(new BigDecimal("4.0"))
                .endBalance(new BigDecimal("1.0"))
                .mutation(new BigDecimal("+2.0")).build();
        TransactionData dataFour = TransactionData.builder()
                .referenceNo(22L)
                .accountNo("NL91 ABNA 0417 1643 23")
                .startBalance(new BigDecimal("4.0"))
                .endBalance(new BigDecimal("6.0"))
                .mutation(new BigDecimal("+2.0")).build();
        List<TransactionData> transactionData = new ArrayList<>();
        transactionData.add(data);
        transactionData.add(dataTwo);
        transactionData.add(dataThree);
        transactionData.add(dataFour);
        when(bankStatementDao.checkTransactionRecordExists(20L)).thenReturn(true);
        when(bankStatementDao.checkTransactionRecordExists(21L)).thenReturn(true);
        response = bankStatementService.processTransactionRecords(transactionData);
        assertEquals("DUPLICATE_REFERENCE_INCORRECT_END_BALANCE", response.getResult());
        assertEquals(5, response.getErrorRecords().size());
    }

    @Test
    public void testProcessCsvFile() throws IOException {
        when(multipartFile.getInputStream()).thenReturn(getClass().getResourceAsStream("/records.csv"));
        StatementResponse response = bankStatementService.processCsvFile(multipartFile);
        assertEquals("SUCCESSFUL", response.getResult());
    }

    @Test
    public void testProcessXmlFile() throws IOException {
        when(multipartFile.getInputStream()).thenReturn(getClass().getResourceAsStream("/records.xml"));
        StatementResponse response = bankStatementService.processXmlFile(multipartFile);
        assertEquals("SUCCESSFUL", response.getResult());
    }

    @Test
    public void checkDBIsAvailable() {
        boolean isDBAvailable = bankStatementService.checkDBIsAvailable();
        assertTrue(isDBAvailable);
    }

    @Test
    public void checkDBIsNotAvailable() {
        when(bankStatementDao.checkTransactionRecordExists(1L)).thenThrow(new RuntimeException("DB is down"));
        boolean isDBAvailable = bankStatementService.checkDBIsAvailable();
        assertFalse(isDBAvailable);
    }

}
