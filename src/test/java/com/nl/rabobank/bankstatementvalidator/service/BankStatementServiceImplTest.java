package com.nl.rabobank.bankstatementvalidator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

import com.nl.rabobank.bankstatementvalidator.dao.BankStatementDao;
import com.nl.rabobank.bankstatementvalidator.domain.StatementResponse;
import com.nl.rabobank.bankstatementvalidator.domain.TransactionData;
import com.nl.rabobank.bankstatementvalidator.exception.BankStatementDBException;

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
		TransactionData data = new TransactionData();
		data.setAccountNo("NL91 ABNA 0417 1643 01");
		data.setReferenceNo(1);
		data.setStartBalance(2.0);
		data.setEndBalance(4.0);
		data.setMutation("+2.0");
	
		TransactionData dataTwo = new TransactionData();
		dataTwo.setAccountNo("NL91 BANA 0417 1643 02");
		dataTwo.setReferenceNo(2);
		dataTwo.setStartBalance(1.0);
		dataTwo.setEndBalance(2.0);
		dataTwo.setMutation("+1.0");
		
		List<TransactionData> transactionData =  new ArrayList<>();
		transactionData.add(data);
		transactionData.add(dataTwo);
		
		when(bankStatementDao.checkTransactionRecordExists(Mockito.anyInt())).thenReturn(false);
		response = bankStatementService.processTransactionRecords(transactionData);
		assertEquals("SUCCESSFUL", response.getResult());
		assertEquals(0, response.getErrorRecords().size());
	}
	
	@Test
	void testProcessMethodForDuplicateReference() {
		TransactionData data = new TransactionData();
		data.setAccountNo("NL91 ABNA 0417 1643 01");
		data.setReferenceNo(1);
		data.setStartBalance(2.0);
		data.setEndBalance(4.0);
		data.setMutation("+2.0");
	
		TransactionData dataTwo = new TransactionData();
		dataTwo.setAccountNo("NL91 BANA 0417 1643 02");
		dataTwo.setReferenceNo(2);
		dataTwo.setStartBalance(1.0);
		dataTwo.setEndBalance(2.0);
		dataTwo.setMutation("+1.0");
		
		List<TransactionData> transactionData =  new ArrayList<>();
		transactionData.add(data);
		transactionData.add(dataTwo);
		
		when(bankStatementDao.checkTransactionRecordExists(1)).thenReturn(true);
		response = bankStatementService.processTransactionRecords(transactionData);
		assertEquals("DUPLICATE_REFERENCE", response.getResult());
		assertEquals(1, response.getErrorRecords().size());
	}
	
	@Test
	void testProcessMethodForInCorrectEndBalance() {
		TransactionData data = new TransactionData();
		data.setAccountNo("NL91 ABNA 0417 1643 01");
		data.setReferenceNo(4);
		data.setStartBalance(4.0);
		data.setEndBalance(1.0);
		data.setMutation("-2.0");
	
		List<TransactionData> transactionData =  new ArrayList<>();
		transactionData.add(data);
		
		when(bankStatementDao.checkTransactionRecordExists(4)).thenReturn(false);
		response = bankStatementService.processTransactionRecords(transactionData);
		assertEquals("INCORRECT_END_BALANCE", response.getResult());
		assertEquals(1, response.getErrorRecords().size());
	}
	
	@Test
	void testProcessMethodForBothDuplicateAndInCorrectBalance() {
		TransactionData data = new TransactionData();
		data.setAccountNo("NL91 ABNA 0417 1643 01");
		data.setReferenceNo(5);
		data.setStartBalance(4.0);
		data.setEndBalance(1.0);
		data.setMutation("-1.0");
	
		List<TransactionData> transactionData =  new ArrayList<>();
		transactionData.add(data);
		
		when(bankStatementDao.checkTransactionRecordExists(5)).thenReturn(true);
		response = bankStatementService.processTransactionRecords(transactionData);
		assertEquals("DUPLICATE_REFERENCE_INCORRECT_END_BALANCE", response.getResult());
		assertEquals(2, response.getErrorRecords().size());
	}
	
	@Test
	void testProcessMethodForInternalServerError() {
		TransactionData data = new TransactionData();
		data.setAccountNo("NL91 ABNA 0417 1643 15");
		data.setReferenceNo(6);
		data.setStartBalance(4.0);
		data.setEndBalance(2.0);
		data.setMutation("-2.0");
	
		List<TransactionData> transactionData =  new ArrayList<>();
		transactionData.add(data);
		
		when(bankStatementDao.checkTransactionRecordExists(6)).thenThrow(new RuntimeException("DB is not available"));
		assertThrows(BankStatementDBException.class, () -> bankStatementService.processTransactionRecords(transactionData));
		
	}
	
	//this test case covers the input json itself contains duplicates and also have duplicates when invoke db and Incorrect balance
	@Test
	void testWhenBothDuplicateAndInCorrectBalanceForMultipleRec() {
		TransactionData data = new TransactionData();
		data.setAccountNo("NL91 ABNA 0417 1643 20");
		data.setReferenceNo(20);
		data.setStartBalance(4.0);
		data.setEndBalance(1.0);
		data.setMutation("-2.0");
		
		TransactionData dataTwo = new TransactionData();
		dataTwo.setAccountNo("NL91 ABNA 0417 1643 21");
		dataTwo.setReferenceNo(21);
		dataTwo.setStartBalance(4.0);
		dataTwo.setEndBalance(5.0);
		dataTwo.setMutation("+1.0");
		
		TransactionData dataThree = new TransactionData();
		dataThree.setAccountNo("NL91 ABNA 0417 1643 22");
		dataThree.setReferenceNo(22);
		dataThree.setStartBalance(4.0);
		dataThree.setEndBalance(1.0);
		dataThree.setMutation("+2.0");
		
		TransactionData dataFour = new TransactionData();
		dataFour.setAccountNo("NL91 ABNA 0417 1643 23");
		dataFour.setReferenceNo(22);
		dataFour.setStartBalance(4.0);
		dataFour.setEndBalance(6.0);
		dataFour.setMutation("+2.0");
	
		List<TransactionData> transactionData =  new ArrayList<>();
		transactionData.add(data);
		transactionData.add(dataTwo);
		transactionData.add(dataThree);
		transactionData.add(dataFour);
		
		when(bankStatementDao.checkTransactionRecordExists(20)).thenReturn(true);
		when(bankStatementDao.checkTransactionRecordExists(21)).thenReturn(true);
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
        boolean  isDBAvailable = bankStatementService.checkDBIsAvailable();
        assertTrue(isDBAvailable);
    }
	
	@Test
    public void checkDBIsNotAvailable() {
		when(bankStatementDao.checkTransactionRecordExists(1)).thenThrow(new RuntimeException("DB is down"));
        boolean  isDBAvailable = bankStatementService.checkDBIsAvailable();
        assertFalse(isDBAvailable);
    }
	


}
