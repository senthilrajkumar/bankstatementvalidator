package com.nl.rabobank.bankstatementvalidator;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.nl.rabobank.bankstatementvalidator.controller.BankStatementController;

@SpringBootTest(classes = BankstatementvalidatorApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class VerificationControllerIntegrationTest {

	@LocalServerPort
	private int port;

	private MockMvc mockMvc;

	private InputStream inputStream;

	@Autowired
	BankStatementController bankStatementController;

	@BeforeEach
	public void setup() throws Exception {
		this.mockMvc = standaloneSetup(this.bankStatementController).build();
	}

	@Sql({ "classpath:schema.sql" })
	@Test
	@Order(1)
	public void testCsvRecordsForSuccesssfulScenario() throws Exception {
		inputStream = getClass().getClassLoader().getResourceAsStream("records.csv");
		String fileName = "records.csv";
		MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/vnd.ms-excel", inputStream);
		MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
				.multipart("/statement/v1/uploadCsv");
		mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/vnd.ms-excel"))
				.andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.result").value("SUCCESSFUL"));
	}

	@Test
	@Order(2)
	void testAPIForDuplicateCase() throws Exception {
		inputStream = getClass().getClassLoader().getResourceAsStream("records-duplicate.csv");
		String fileName = "records-duplicate.csv";
		MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/vnd.ms-excel", inputStream);
		MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
				.multipart("/statement/v1/uploadCsv");
		mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/vnd.ms-excel"))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.result").value("DUPLICATE_REFERENCE"));
	}

	@Test
	@Order(3)
	void testAPIForInCorrectEndBalanceCase() throws Exception {
		inputStream = getClass().getClassLoader().getResourceAsStream("records-incorrectbalance.csv");
		String fileName = "records-incorrectbalance.csv";
		MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/vnd.ms-excel", inputStream);
		MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
				.multipart("/statement/v1/uploadCsv");
		mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/vnd.ms-excel"))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.result").value("INCORRECT_END_BALANCE"));

	}

	@Test
	@Order(4)
	void testAPIForInCorrectEndBalanceAndDuplicateReference() throws Exception {
		inputStream = getClass().getClassLoader().getResourceAsStream("records-incorrectbalance-duplicate.csv");
		String fileName = "records-incorrectbalance-duplicate.csv";
		MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/vnd.ms-excel", inputStream);
		MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
				.multipart("/statement/v1/uploadCsv");
		mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/vnd.ms-excel"))
				.andExpect(status().isOk()).andExpect(
						MockMvcResultMatchers.jsonPath("$.result").value("DUPLICATE_REFERENCE_INCORRECT_END_BALANCE"));

	}

	@Test
	@Order(5)
	public void testXmlFileRecords() throws Exception {
		String fileName = "records.xml";
		inputStream = getClass().getClassLoader().getResourceAsStream("records.xml");
		MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/xml", inputStream);
		MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
				.multipart("/statement/v1/uploadXml");
		mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/xml")).andExpect(status().isOk());
	}

	@Test
	@Order(6)
	public void testXmlFileRecordsForDuplicateReference() throws Exception {
		String fileName = "records-duplicate.xml";
		inputStream = getClass().getClassLoader().getResourceAsStream("records-duplicate.xml");
		MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/xml", inputStream);
		MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
				.multipart("/statement/v1/uploadXml");
		mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/xml")).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.result").value("DUPLICATE_REFERENCE"));
	}

	@Test
	@Order(7)
	public void testXmlFileRecordsForIncorrectBalance() throws Exception {
		String fileName = "records-incorrectbalance.xml";
		inputStream = getClass().getClassLoader().getResourceAsStream("records-incorrectbalance.xml");
		MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/xml", inputStream);
		MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
				.multipart("/statement/v1/uploadXml");
		mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/xml")).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("$.result").value("INCORRECT_END_BALANCE"));
	}

	@Test
	@Order(8)
	public void testXmlFileRecordsForIncorrectBalanceAndDuplicate() throws Exception {
		String fileName = "records-incorrectbalance-duplicate.xml";
		inputStream = getClass().getClassLoader().getResourceAsStream("records-incorrectbalance-duplicate.xml");
		MockMultipartFile sampleFile = new MockMultipartFile("file", fileName, "application/xml", inputStream);
		MockMultipartHttpServletRequestBuilder multipartRequest = MockMvcRequestBuilders
				.multipart("/statement/v1/uploadXml");
		mockMvc.perform(multipartRequest.file(sampleFile).contentType("application/xml")).andExpect(status().isOk())
				.andExpect(
						MockMvcResultMatchers.jsonPath("$.result").value("DUPLICATE_REFERENCE_INCORRECT_END_BALANCE"));
	}

	@Test
	@Order(5)
	void testAppHealthCheckAPI() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/statement/v2/app-health-check")).andExpect(status().isOk())
				.andExpect(content().string(containsString("Application is Up and Running")));
	}

	@Test
	@Order(6)
	void testDBHealthCheckAPI() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/statement/v2/db-health-check")).andExpect(status().isOk())
				.andExpect(content().string(containsString("DB is Up and Running")));
	}

}
