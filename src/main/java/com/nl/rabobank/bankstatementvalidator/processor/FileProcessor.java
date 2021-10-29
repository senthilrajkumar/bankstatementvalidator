package com.nl.rabobank.bankstatementvalidator.processor;

import java.io.InputStream;
import java.util.List;

import com.nl.rabobank.bankstatementvalidator.domain.TransactionData;

public interface FileProcessor {
	
	 List<TransactionData> process(InputStream inputStream);

}
