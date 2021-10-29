package com.nl.rabobank.bankstatementvalidator.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nl.rabobank.bankstatementvalidator.constant.ApplicationConstant;
import com.nl.rabobank.bankstatementvalidator.domain.StatementResponse;
import com.nl.rabobank.bankstatementvalidator.service.BankStatementService;
import com.nl.rabobank.bankstatementvalidator.util.CommonUtil;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/statement")
@Validated
public class BankStatementController {

	private static final Logger log = LoggerFactory.getLogger(BankStatementController.class);

	@Autowired
	BankStatementService bankStatementService;

	@ApiOperation(value = "process Bank Statement Transaction CSV file Record Details", response = StatementResponse.class)
	@PostMapping(path = "/v1/uploadCsv")
	public ResponseEntity<StatementResponse> uploadCSVFile(@RequestParam("file") MultipartFile file)
			throws IOException {
		String message = "";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		if (CommonUtil.hasCSVFormat(file)) {
			StatementResponse response = bankStatementService.processCsvFile(file);
			return new ResponseEntity<StatementResponse>(response, headers, HttpStatus.OK);
		}
		message = ApplicationConstant.CSV_FILE;
		StatementResponse response = new StatementResponse();
		response.setResult(message);

		return new ResponseEntity<StatementResponse>(response, headers, HttpStatus.BAD_REQUEST);
	}

	@ApiOperation(value = "process Bank Statement Transaction XML file Record Details", response = StatementResponse.class)
	@PostMapping(path = "/v1/uploadXml")
	public ResponseEntity<StatementResponse> uploadXMLFile(@RequestParam("file") MultipartFile file)
			throws IOException {
		String message = "";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		if (CommonUtil.hasXMLFormat(file)) {
			StatementResponse response = bankStatementService.processXmlFile(file);
			return new ResponseEntity<StatementResponse>(response, headers, HttpStatus.OK);
		}
		message = ApplicationConstant.XML_FILE;
		StatementResponse response = new StatementResponse();
		response.setResult(message);

		return new ResponseEntity<StatementResponse>(response, headers, HttpStatus.BAD_REQUEST);
	}

	@ApiOperation(value = "Application Health Check API", response = String.class)
	@GetMapping(value = "/v2/app-health-check", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> performAppHealthCheck() {
		log.info("performAppHealthCheck method --->");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<String>(ApplicationConstant.APPLICATION_IS_UP_AND_RUNNING, headers, HttpStatus.OK);
	}

	@ApiOperation(value = "Database Health Check API", response = String.class)
	@GetMapping(value = "/v2/db-health-check", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> performDbHealthCheck() {
		log.info("performDbHealthCheck method --->");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		if (bankStatementService.checkDBIsAvailable()) {
			log.info("DB is Up and Running  --->");
			return new ResponseEntity<String>(ApplicationConstant.DB_IS_UP_AND_RUNNING, headers, HttpStatus.OK);
		} else {
			log.info("DB is Down  --->");
			return new ResponseEntity<String>(ApplicationConstant.DB_IS_DOWN, headers,
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}
