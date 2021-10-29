package com.nl.rabobank.bankstatementvalidator.service;

import com.nl.rabobank.bankstatementvalidator.domain.StatementResponse;
import com.nl.rabobank.bankstatementvalidator.domain.TransactionData;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface BankStatementService {

    StatementResponse processTransactionRecords(List<TransactionData> records);

    boolean checkDBIsAvailable();

    StatementResponse processCsvFile(MultipartFile file) throws IOException;

    StatementResponse processXmlFile(MultipartFile file) throws IOException;

}
