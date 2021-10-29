package com.nl.rabobank.bankstatementvalidator.controller;

import com.nl.rabobank.bankstatementvalidator.constant.ApplicationConstant;
import com.nl.rabobank.bankstatementvalidator.domain.StatementResponse;
import com.nl.rabobank.bankstatementvalidator.service.BankStatementService;
import com.nl.rabobank.bankstatementvalidator.util.CommonUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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

import java.io.IOException;

@RestController
@RequestMapping("/statement")
@Validated
@Slf4j
public class BankStatementController {
    @Autowired
    protected BankStatementService bankStatementService;

    @ApiOperation(value = "process Bank Statement Transaction CSV file Record Details", response = StatementResponse.class)
    @PostMapping(path = "/uploadCsv")
    public ResponseEntity<StatementResponse> uploadCSVFile(@RequestParam("file") MultipartFile file)
            throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (CommonUtil.hasCSVFormat(file)) {
            StatementResponse response = bankStatementService.processCsvFile(file);
            return new ResponseEntity<>(response, headers, HttpStatus.OK);
        }
        log.info("wrong file format was uploaded");
        StatementResponse response = new StatementResponse();
        response.setResult(ApplicationConstant.CSV_FILE);
        return new ResponseEntity<>(response, headers, HttpStatus.BAD_REQUEST);
    }

    @ApiOperation(value = "process Bank Statement Transaction XML file Record Details", response = StatementResponse.class)
    @PostMapping(path = "/uploadXml")
    public ResponseEntity<StatementResponse> uploadXMLFile(@RequestParam("file") MultipartFile file)
            throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (CommonUtil.hasXMLFormat(file)) {
            StatementResponse response = bankStatementService.processXmlFile(file);
            return new ResponseEntity<>(response, headers, HttpStatus.OK);
        }
        log.info("wrong file format was uploaded");
        StatementResponse response = new StatementResponse();
        response.setResult(ApplicationConstant.XML_FILE);
        return new ResponseEntity<>(response, headers, HttpStatus.BAD_REQUEST);
    }

    @ApiOperation(value = "Application Health Check API", response = String.class)
    @GetMapping(value = "/app-health-check", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> performAppHealthCheck() {
        log.info("performAppHealthCheck method --->");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(ApplicationConstant.APPLICATION_IS_UP_AND_RUNNING, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "Database Health Check API", response = String.class)
    @GetMapping(value = "/db-health-check", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> performDbHealthCheck() {
        log.info("performDbHealthCheck method --->");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (bankStatementService.checkDBIsAvailable()) {
            log.info("DB is Up and Running  --->");
            return new ResponseEntity<>(ApplicationConstant.DB_IS_UP_AND_RUNNING, headers, HttpStatus.OK);
        } else {
            log.info("DB is Down  --->");
            return new ResponseEntity<>(ApplicationConstant.DB_IS_DOWN, headers,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
