package com.nl.rabobank.bankstatementvalidator.dao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.nl.rabobank.bankstatementvalidator.domain.TransactionData;
import com.nl.rabobank.bankstatementvalidator.repository.BankStatementRepository;

@SpringBootTest
class BankStatementDaoImplTest {
	
	@MockBean
	private BankStatementRepository repository;
	
	@Autowired
	private BankStatementDao bankStatementDao;

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testPersistTransactionRecords() throws Exception {
		TransactionData data = new TransactionData();
		data.setAccountNo("NL91 ABNA 0417 1643 25");
		data.setReferenceNo(25);
		data.setStartBalance(2.0);
		data.setEndBalance(4.0);
		data.setMutation("+2.0");
		bankStatementDao.persistTransactionRecords(data);
		verify(repository, times(1)).save(data);
	}
	
	@Test
	void testCheckTransactionRecordWhenExistsInDB() throws Exception {
		TransactionData data = new TransactionData();
		data.setAccountNo("NL91 ABNA 0417 1643 26");
		data.setReferenceNo(26);
		data.setStartBalance(2.0);
		data.setEndBalance(4.0);
		data.setMutation("+2.0");
		when(repository.findByReferenceNo(26)).thenReturn(data);
		boolean recordExist = bankStatementDao.checkTransactionRecordExists(26);
		assertTrue(recordExist);
	}
	
	@Test
	void testCheckTransactionRecordWhenDoesNotExistInDB() throws Exception {
		when(repository.findByReferenceNo(27)).thenReturn(null);
		boolean recordExist = bankStatementDao.checkTransactionRecordExists(27);
		assertFalse(recordExist);
	}

}