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
		validateXmlReference(xmlRecord, record);

		String accountNumber = xmlRecord.getAccountNumber();
		validateXmlAccountNumber(record, accountNumber);

		record.setDescription(xmlRecord.getDescription());

		validateXmlStartBalance(xmlRecord, record);

		validateXmlMutation(xmlRecord, record);

		validateXmlEndBalance(xmlRecord, record);

		return record;
	}

	private void validateXmlEndBalance(XMLStatementRecord xmlRecord, TransactionData record) {
		String endBalanceFromXml = xmlRecord.getEndBalance();
		if (StringUtils.hasText(endBalanceFromXml)) {
			record.setEndBalance(Double.parseDouble(endBalanceFromXml));
		} else {
			throw new TransactionDataInputException(ApplicationConstant.END_BALANCE_IS_MANDATORY);
		}
	}

	private void validateXmlMutation(XMLStatementRecord xmlRecord, TransactionData record) {
		String mutationFromXml = xmlRecord.getMutation();
		if (StringUtils.hasText(mutationFromXml)) {
			record.setMutation(mutationFromXml);
		} else {
			throw new TransactionDataInputException(ApplicationConstant.MUTATION_IS_MANDATORY);
		}
	}

	private void validateXmlStartBalance(XMLStatementRecord xmlRecord, TransactionData record) {
		String startBalanceFromXml = xmlRecord.getStartBalance();
		if (StringUtils.hasText(startBalanceFromXml)) {
			record.setStartBalance(Double.parseDouble(startBalanceFromXml));
		} else {
			throw new TransactionDataInputException(ApplicationConstant.START_BALANCE_IS_MANDATORY);
		}
	}

	private void validateXmlAccountNumber(TransactionData record, String accountNumberFromXml) {
		if (StringUtils.hasText(accountNumberFromXml)) {

			boolean isIBAN = Pattern.compile(ApplicationConstant.REGEX_IBAN).matcher(accountNumberFromXml).matches();
			if (!isIBAN) {
				throw new TransactionDataInputException(ApplicationConstant.ACCOUNT_NUMBER_FORMAT);
			} else {
				record.setAccountNo(accountNumberFromXml);
			}
		} else {
			throw new TransactionDataInputException(ApplicationConstant.ACCOUNT_NUMBER_IS_MANDATORY);
		}
	}

	private void validateXmlReference(XMLStatementRecord xmlRecord, TransactionData record) {
		String referenceFromXml = xmlRecord.getReference();
		if (StringUtils.hasText(referenceFromXml)) {
			record.setReferenceNo(Integer.parseInt(referenceFromXml));
		} else {
			throw new TransactionDataInputException(ApplicationConstant.TRANSACTION_REFERENCE_IS_MANDATORY);
		}
	}

}
