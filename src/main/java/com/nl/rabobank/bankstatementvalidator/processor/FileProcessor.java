package com.nl.rabobank.bankstatementvalidator.processor;

import com.nl.rabobank.bankstatementvalidator.domain.TransactionData;

import java.io.InputStream;
import java.util.List;

public interface FileProcessor {

    List<TransactionData> process(InputStream inputStream);

}
