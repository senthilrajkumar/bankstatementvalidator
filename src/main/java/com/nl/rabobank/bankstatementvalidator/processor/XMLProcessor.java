package com.nl.rabobank.bankstatementvalidator.processor;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.nl.rabobank.bankstatementvalidator.constant.ApplicationConstant;
import com.nl.rabobank.bankstatementvalidator.domain.TransactionData;
import com.nl.rabobank.bankstatementvalidator.domain.XMLStatementRecord;
import com.nl.rabobank.bankstatementvalidator.domain.XMLStatementRecords;
import com.nl.rabobank.bankstatementvalidator.exception.TransactionDataInputException;

@Component
public class XMLProcessor implements FileProcessor {

	private static final Logger log = LoggerFactory.getLogger(XMLProcessor.class);

	@Override
	public List<TransactionData> process(InputStream inputStream) {
		ObjectMapper xmlMapper = new XmlMapper();
		xmlMapper.registerModule(new ParameterNamesModule());
		xmlMapper.registerModule(new Jdk8Module());
		xmlMapper.registerModule(new JavaTimeModule());

		XMLStatementRecords statementRecords;
		try {
			statementRecords = xmlMapper.readValue(inputStream, XMLStatementRecords.class);
		} catch (IOException | IllegalArgumentException e) {
			log.error("Exception Occured {}", e.getMessage());
			throw new RuntimeException("fail to parse XML file: " + e.getMessage());
		}
		return convert(statementRecords);
	}

	private List<TransactionData> convert(XMLStatementRecords statementRecords) {
		return statementRecords.getXmlRecords().stream().map(this::mapXmlStatementRecord).collect(Collectors.toList());
	}

	private TransactionData mapXmlStatementRecord(XMLStatementRecord xmlRecord) {

		TransactionData record = new TransactionData();
		String reference = xmlRecord.getReference();
		if (StringUtils.hasText(reference))
			record.setReferenceNo(Integer.parseInt(reference));
		else
			throw new TransactionDataInputException(ApplicationConstant.TRANSACTION_REFERENCE_IS_MANDATORY);

		String accountNumber = xmlRecord.getAccountNumber();
		if (StringUtils.hasText(accountNumber)) {

			boolean isIBAN = Pattern.compile(ApplicationConstant.REGEX_IBAN).matcher(accountNumber).matches();
			if (!isIBAN)
				throw new TransactionDataInputException(ApplicationConstant.ACCOUNT_NUMBER_FORMAT);
			else
				record.setAccountNo(accountNumber);
		} else
			throw new TransactionDataInputException(ApplicationConstant.ACCOUNT_NUMBER_IS_MANDATORY);

		record.setDescription(xmlRecord.getDescription());

		String startBalance = xmlRecord.getStartBalance();
		if (StringUtils.hasText(startBalance))
			record.setStartBalance(Double.parseDouble(startBalance));
		else
			throw new TransactionDataInputException(ApplicationConstant.START_BALANCE_IS_MANDATORY);

		String mutation = xmlRecord.getMutation();
		if (StringUtils.hasText(mutation))
			record.setMutation(mutation);
		else
			throw new TransactionDataInputException(ApplicationConstant.MUTATION_IS_MANDATORY);

		String endBalance = xmlRecord.getEndBalance();
		if (StringUtils.hasText(endBalance))
			record.setEndBalance(Double.parseDouble(endBalance));
		else
			throw new TransactionDataInputException(ApplicationConstant.END_BALANCE_IS_MANDATORY);

		return record;
	}

}
