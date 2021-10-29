package com.nl.rabobank.bankstatementvalidator.dao;

import com.nl.rabobank.bankstatementvalidator.domain.TransactionData;
import com.nl.rabobank.bankstatementvalidator.repository.BankStatementRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class BankStatementDaoImplTest {

    @MockBean
    private BankStatementRepository repository;

    @Autowired
    private BankStatementDao bankStatementDao;

    @Test
    void testPersistTransactionRecords() {
        TransactionData data = TransactionData.builder()
                .accountNo("NL91 ABNA 0417 1643 25")
                .referenceNo(25L)
                .startBalance(new BigDecimal("2.0"))
                .endBalance(new BigDecimal("4.0"))
                .mutation(new BigDecimal("+2.0")).build();
        bankStatementDao.persistTransactionRecords(data);
        verify(repository, times(1)).save(data);
    }

    @Test
    void testCheckTransactionRecordWhenExistsInDB() {
        TransactionData data = TransactionData.builder()
                .accountNo("NL91 ABNA 0417 1643 26")
                .referenceNo(26L)
                .startBalance(new BigDecimal("2.0"))
                .endBalance(new BigDecimal("4.0"))
                .mutation(new BigDecimal("+2.0")).build();
        when(repository.findByReferenceNo(26L)).thenReturn(data);
        boolean recordExist = bankStatementDao.checkTransactionRecordExists(26L);
        assertTrue(recordExist);
    }

    @Test
    void testCheckTransactionRecordWhenDoesNotExistInDB() {
        when(repository.findByReferenceNo(27L)).thenReturn(null);
        boolean recordExist = bankStatementDao.checkTransactionRecordExists(27L);
        assertFalse(recordExist);
    }

}
