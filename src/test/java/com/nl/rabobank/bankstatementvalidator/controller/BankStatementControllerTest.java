package com.nl.rabobank.bankstatementvalidator.controller;

import com.nl.rabobank.bankstatementvalidator.domain.StatementResponse;
import com.nl.rabobank.bankstatementvalidator.exception.BankStatementDBException;
import com.nl.rabobank.bankstatementvalidator.exception.TransactionDataInputException;
import com.nl.rabobank.bankstatementvalidator.service.BankStatementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankStatementController.class)
public class BankStatementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BankStatementService bankStatementService;

    private InputStream inputStream;

    @Test
    protected void testAppHealthCheckAPI() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/statement/app-health-check")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Application is Up and Running")));
    }

    @Test
    protected void testDBHealthCheckAPIWhenDBDown() throws Exception {
        when(bankStatementService.checkDBIsAvailable()).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.get("/statement/db-health-check"))
                .andExpect(status().is5xxServerError())
                .andExpect(content().string(containsString("DB is Not Available")));
    }

    @Test
    protected void testDBHealthCheckAPIWhenDBIsAvailable() throws Exception {
        when(bankStatementService.checkDBIsAvailable()).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.get("/statement/db-health-check")).andExpect(status().isOk())
                .andExpect(content().string(containsString("DB is Up and Running")));
    }

    @Test
    protected void testCsvFileRecords() throws Exception {
        String fileName = "records.csv";
        inputStream = getClass().getClassLoader().getResourceAsStream("records.csv");
        MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/vnd.ms-excel", inputStream);
        StatementResponse response = new StatementResponse();
        MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
                .multipart("/statement/uploadCsv");
        when(bankStatementService.processCsvFile(sampleFile)).thenReturn(response);
        mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/vnd.ms-excel"))
                .andExpect(status().isOk());
    }

    @Test
    protected void testCsvFileRecordsForDuplicateReference() throws Exception {
        String fileName = "records-duplicate.csv";
        inputStream = getClass().getClassLoader().getResourceAsStream("records-duplicate.csv");
        MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/vnd.ms-excel", inputStream);
        StatementResponse response = new StatementResponse();
        response.setResult("DUPLICATE_REFERENCE");
        MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
                .multipart("/statement/uploadCsv");
        when(bankStatementService.processCsvFile(sampleFile)).thenReturn(response);
        mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/vnd.ms-excel"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("DUPLICATE_REFERENCE"));
    }

    @Test
    protected void testCsvFileRecordsForIncorrectBalance() throws Exception {
        String fileName = "records-incorrectbalance.csv";
        inputStream = getClass().getClassLoader().getResourceAsStream("records-incorrectbalance.csv");
        MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/vnd.ms-excel", inputStream);
        StatementResponse response = new StatementResponse();
        response.setResult("INCORRECT_END_BALANCE");
        MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
                .multipart("/statement/uploadCsv");
        when(bankStatementService.processCsvFile(sampleFile)).thenReturn(response);
        mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/vnd.ms-excel"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("INCORRECT_END_BALANCE"));
    }

    @Test
    protected void testCsvFileRecordsForIncorrectBalanceAndDuplicate() throws Exception {
        String fileName = "records-incorrectbalance-duplicate.csv";
        inputStream = getClass().getClassLoader().getResourceAsStream("records-incorrectbalance-duplicate.csv");
        MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/vnd.ms-excel", inputStream);
        StatementResponse response = new StatementResponse();
        response.setResult("DUPLICATE_REFERENCE_INCORRECT_END_BALANCE");
        MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
                .multipart("/statement/uploadCsv");
        when(bankStatementService.processCsvFile(sampleFile)).thenReturn(response);
        mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/vnd.ms-excel"))
                .andExpect(status().isOk()).andExpect(
                        MockMvcResultMatchers.jsonPath("$.result").value("DUPLICATE_REFERENCE_INCORRECT_END_BALANCE"));
    }

    @Test
    protected void testCsvFilerecordsWhenInternalServerErrorFromService() throws Exception {
        String fileName = "records-test.csv";
        inputStream = getClass().getClassLoader().getResourceAsStream("records-test.csv");
        MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/vnd.ms-excel", inputStream);
        MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
                .multipart("/statement/uploadCsv");
        when(bankStatementService.processCsvFile(sampleFile))
                .thenThrow(new BankStatementDBException("Db is not available"));
        mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/vnd.ms-excel"))
                .andExpect(status().is5xxServerError()).andExpect(MockMvcResultMatchers.jsonPath("$.result").value("INTERNAL_SERVER_ERROR"));
    }

    // Start Balance is not being passed
    @Test
    protected void testCsvFilerecordsForImproperInput() throws Exception {
        String fileName = "records-input-missing-startbalance.csv";
        inputStream = getClass().getClassLoader().getResourceAsStream("records-input-missing-startbalance.csv");
        MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/vnd.ms-excel", inputStream);
        MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
                .multipart("/statement/uploadCsv");
        when(bankStatementService.processCsvFile(sampleFile))
                .thenThrow(new TransactionDataInputException("Start Balance is mandatory"));
        mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/vnd.ms-excel"))
                .andExpect(status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("BAD_REQUEST")).andExpect(content().string(containsString("Start Balance is mandatory")));
    }

    // Account Number IBAN format is not right
    @Test
    protected void testCsvFileRecordsForImproperAccountNo() throws Exception {
        String fileName = "records-improper-account-number.csv";
        inputStream = getClass().getClassLoader().getResourceAsStream("records-improper-account-number.csv");
        MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/vnd.ms-excel", inputStream);
        MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
                .multipart("/statement/uploadCsv");
        when(bankStatementService.processCsvFile(sampleFile))
                .thenThrow(new TransactionDataInputException("Account Number should be in the right format"));
        mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/vnd.ms-excel"))
                .andExpect(status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("BAD_REQUEST")).andExpect(content().string(containsString("Account Number should be in the right format")));
    }

    @Test
    protected void testXmlFileRecords() throws Exception {
        String fileName = "records.xml";
        inputStream = getClass().getClassLoader().getResourceAsStream("records.xml");
        MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/xml", inputStream);
        StatementResponse response = new StatementResponse();
        MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
                .multipart("/statement/uploadXml");
        when(bankStatementService.processXmlFile(sampleFile)).thenReturn(response);
        mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/xml"))
                .andExpect(status().isOk());
    }

    @Test
    protected void testXmlFileRecordsForDuplicateReference() throws Exception {
        String fileName = "records-duplicate.xml";
        inputStream = getClass().getClassLoader().getResourceAsStream("records-duplicate.xml");
        MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/xml", inputStream);
        StatementResponse response = new StatementResponse();
        response.setResult("DUPLICATE_REFERENCE");
        MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
                .multipart("/statement/uploadXml");
        when(bankStatementService.processXmlFile(sampleFile)).thenReturn(response);
        mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/xml"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("DUPLICATE_REFERENCE"));
    }

    @Test
    protected void testXmlFileRecordsForIncorrectBalance() throws Exception {
        String fileName = "records-incorrectbalance.xml";
        inputStream = getClass().getClassLoader().getResourceAsStream("records-incorrectbalance.xml");
        MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/xml", inputStream);
        StatementResponse response = new StatementResponse();
        response.setResult("INCORRECT_END_BALANCE");
        MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
                .multipart("/statement/uploadXml");
        when(bankStatementService.processXmlFile(sampleFile)).thenReturn(response);
        mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/xml"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("INCORRECT_END_BALANCE"));
    }

    @Test
    protected void testXmlFileRecordsForIncorrectBalanceAndDuplicate() throws Exception {
        String fileName = "records-incorrectbalance-duplicate.xml";
        inputStream = getClass().getClassLoader().getResourceAsStream("records-incorrectbalance-duplicate.xml");
        MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/xml", inputStream);
        StatementResponse response = new StatementResponse();
        response.setResult("DUPLICATE_REFERENCE_INCORRECT_END_BALANCE");
        MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
                .multipart("/statement/uploadXml");
        when(bankStatementService.processXmlFile(sampleFile)).thenReturn(response);
        mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/xml"))
                .andExpect(status().isOk()).andExpect(
                        MockMvcResultMatchers.jsonPath("$.result").value("DUPLICATE_REFERENCE_INCORRECT_END_BALANCE"));
    }

    @Test
    protected void testCsvFileRecordsWhenOtherFilesAreUploaded() throws Exception {
        String fileName = "records.txt";
        inputStream = getClass().getClassLoader().getResourceAsStream("records.txt");
        MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/text", inputStream);
        StatementResponse response = new StatementResponse();
        MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
                .multipart("/statement/uploadCsv");
        when(bankStatementService.processCsvFile(sampleFile)).thenReturn(response);
        mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/text"))
                .andExpect(status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("Please upload a csv file!"));
    }

    @Test
    protected void testXmlFileRecordsWhenOtherFilesAreUploaded() throws Exception {
        String fileName = "records.txt";
        inputStream = getClass().getClassLoader().getResourceAsStream("records.txt");
        MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/text", inputStream);
        StatementResponse response = new StatementResponse();
        MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
                .multipart("/statement/uploadXml");
        when(bankStatementService.processXmlFile(sampleFile)).thenReturn(response);
        mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/text"))
                .andExpect(status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("Please upload a xml file!"));
    }

}
