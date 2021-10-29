package com.nl.rabobank.bankstatementvalidator.processor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.nl.rabobank.bankstatementvalidator.constant.ApplicationConstant;
import com.nl.rabobank.bankstatementvalidator.domain.CSVStatementRecord;
import com.nl.rabobank.bankstatementvalidator.domain.TransactionData;
import com.nl.rabobank.bankstatementvalidator.exception.TransactionDataInputException;
import com.opencsv.bean.CsvToBeanBuilder;

@Component
public class CSVProcessor implements FileProcessor {
	
	private static final Logger log = LoggerFactory.getLogger(CSVProcessor.class);

	private List<TransactionData> convert(List<CSVStatementRecord> csvStatementRecords) {

		return csvStatementRecords.stream().map(this::mapCsvStatementRecord).collect(Collectors.toList());
	}

	private TransactionData mapCsvStatementRecord(CSVStatementRecord record) {

		TransactionData data = new TransactionData();
		String reference = record.getReference();
		if (StringUtils.hasText(reference))
			data.setReferenceNo(Integer.parseInt(reference));
		else
			throw new TransactionDataInputException(ApplicationConstant.TRANSACTION_REFERENCE_IS_MANDATORY);

		String accountNo = record.getAccountNumber();
		if (StringUtils.hasText(accountNo)) {
			boolean isIBAN = Pattern.compile(ApplicationConstant.REGEX_IBAN).matcher(accountNo).matches();
			if (!isIBAN)
				throw new TransactionDataInputException(ApplicationConstant.ACCOUNT_NUMBER_FORMAT);
			else
				data.setAccountNo(accountNo);
		} else
			throw new TransactionDataInputException(ApplicationConstant.ACCOUNT_NUMBER_IS_MANDATORY);

		data.setDescription(record.getDescription());

		String startBalance = record.getStartBalance();
		if (StringUtils.hasText(startBalance))
			data.setStartBalance(Double.parseDouble(startBalance));
		else
			throw new TransactionDataInputException(ApplicationConstant.START_BALANCE_IS_MANDATORY);

		String mutation = record.getMutation();
		if (StringUtils.hasText(mutation))
			data.setMutation(mutation);
		else
			throw new TransactionDataInputException(ApplicationConstant.MUTATION_IS_MANDATORY);
		String endBalance = record.getEndBalance();
		if (StringUtils.hasText(endBalance))
			data.setEndBalance(Double.parseDouble(endBalance));
		else
			throw new TransactionDataInputException(ApplicationConstant.END_BALANCE_IS_MANDATORY);

		return data;
	}

	@Override
	public List<TransactionData> process(InputStream inputStream) {
		// TODO Auto-generated method stub

		List<CSVStatementRecord> csvStatementRecords;
		try {
			csvStatementRecords = new CsvToBeanBuilder<CSVStatementRecord>(
					new BufferedReader(new InputStreamReader(inputStream))).withOrderedResults(false)
							.withType(CSVStatementRecord.class).build().parse();
		} catch (Exception e) {
			log.error("Exception Occured {}" , e.getMessage());
			throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
		}
		return convert(csvStatementRecords);

	}
}
