package com.nl.rabobank.bankstatementvalidator.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nl.rabobank.bankstatementvalidator.constant.ApplicationConstant;
import com.nl.rabobank.bankstatementvalidator.dao.BankStatementDao;
import com.nl.rabobank.bankstatementvalidator.domain.ErrorRecord;
import com.nl.rabobank.bankstatementvalidator.domain.StatementResponse;
import com.nl.rabobank.bankstatementvalidator.domain.TransactionData;
import com.nl.rabobank.bankstatementvalidator.exception.BankStatementDBException;
import com.nl.rabobank.bankstatementvalidator.processor.CSVProcessor;
import com.nl.rabobank.bankstatementvalidator.processor.XMLProcessor;
import com.nl.rabobank.bankstatementvalidator.util.CommonUtil;

@Service("bankStatementService")
public class BankStatementServiceImpl implements BankStatementService {

	private static final Logger log = LoggerFactory.getLogger(BankStatementServiceImpl.class);

	@Autowired
	private BankStatementDao bankStatementDao;
	
	@Autowired
	private CSVProcessor csvProcessor;
	
	@Autowired
	private XMLProcessor xmlProcessor;
	
	@Override
	public StatementResponse processCsvFile(MultipartFile file) throws IOException {
		// TODO Auto-generated method stub
		log.info("file name {}" , file.getName());
		List<TransactionData> records = csvProcessor.process(file.getInputStream());
		StatementResponse response = this.processTransactionRecords(records);
		log.info("records size {}" , records.size());
		return response;
	}

	@Override
	public StatementResponse processXmlFile(MultipartFile file) throws IOException {
		// TODO Auto-generated method stub
		log.info("file name {}" , file.getName());
		List<TransactionData> records = xmlProcessor.process(file.getInputStream());
		StatementResponse response = this.processTransactionRecords(records);
		log.info("records size {} " ,records.size());
		return response;
	}

	@Override
	public StatementResponse processTransactionRecords(List<TransactionData> records) {
		// TODO Auto-generated method stub
		log.info("processTransactionRecords method {}" , records.size());
		StatementResponse response = new StatementResponse();
		List<ErrorRecord> duplicateErrorRecords = new ArrayList<ErrorRecord>();
		List<ErrorRecord> inCorreectBalanceErrorRecords = new ArrayList<ErrorRecord>();
		List<TransactionData> distinctElements = new ArrayList<TransactionData>();
		List<TransactionData> duplicateInputRecords = new ArrayList<TransactionData>();

		distinctElements = records.stream().filter(CommonUtil.distinctByKey(record -> record.getReferenceNo()))
				.collect(Collectors.toList());
		Set<Integer> inputRecords = new HashSet<Integer>();
		duplicateInputRecords = records.stream().filter(record -> !inputRecords.add(record.getReferenceNo()))
				.collect(Collectors.toList());

		for (TransactionData data : distinctElements) {
			log.info("refNo {}" , data.getReferenceNo());
			try {
				if (bankStatementDao.checkTransactionRecordExists(data.getReferenceNo())) {
					log.info("record exists in DB for refNo {}" , data.getReferenceNo());
					ErrorRecord errRecord = new ErrorRecord();
					errRecord.setAccountNumber(
							data.getAccountNo().concat(ApplicationConstant.OF_DUPLICATE_RECORD));
					errRecord.setReferenceNo(
							data.getReferenceNo().toString().concat(ApplicationConstant.OF_DUPLICATE_RECORD));
					duplicateErrorRecords.add(errRecord);
				} else {
					log.info("record does not exist in DB for refNo {}" , data.getReferenceNo());
					bankStatementDao.persistTransactionRecords(data);
				}
			} catch (Exception ex) {
				log.error("exception occured {}" , ex.getMessage());
				throw new BankStatementDBException(ex.getMessage());
			}
			BigDecimal sum = BigDecimal.valueOf(Double.sum(Double.valueOf(data.getMutation()), data.getStartBalance())).setScale(2, RoundingMode.HALF_UP);
				if (sum.compareTo(BigDecimal.valueOf(data.getEndBalance())) != 0) {
				log.info("inCorrectbalance record {}" , data.getReferenceNo() , " accountNo {}"
						, data.getAccountNo());
				log.info("getMutation {}" , data.getMutation() , " getStartBalance {}"
						, data.getStartBalance() , " getEndBalance {}" , data.getEndBalance());
				ErrorRecord inCorrectbalanceErrRecord = new ErrorRecord();
				inCorrectbalanceErrRecord.setAccountNumber(
						data.getAccountNo().concat(ApplicationConstant.OF_IN_CORRECT_END_BALANCE_RECORD));
				inCorrectbalanceErrRecord.setReferenceNo(
						data.getReferenceNo().toString().concat(ApplicationConstant.OF_IN_CORRECT_END_BALANCE_RECORD));
				inCorreectBalanceErrorRecords.add(inCorrectbalanceErrRecord);
			}
		}
		log.info("First inCorreectBalanceErrorRecords size {}" , inCorreectBalanceErrorRecords.size());
		log.info("distinctElements size {}" , distinctElements.size());
		log.info("duplicateInputRecords size {}" , duplicateInputRecords.size());

		for (TransactionData data : duplicateInputRecords) {
			ErrorRecord errRecord = new ErrorRecord();
			errRecord.setAccountNumber(data.getAccountNo().concat(ApplicationConstant.OF_DUPLICATE_RECORD));
			errRecord.setReferenceNo(data.getReferenceNo().toString().concat(ApplicationConstant.OF_DUPLICATE_RECORD));
			duplicateErrorRecords.add(errRecord);
			BigDecimal sum = BigDecimal.valueOf(Double.sum(Double.valueOf(data.getMutation()), data.getStartBalance())).setScale(2, RoundingMode.HALF_UP);
			if (sum.compareTo(BigDecimal.valueOf(data.getEndBalance())) != 0) {
				log.debug("inCorrectbalance record {}" , data.getReferenceNo() , " accountNo {}"
						, data.getAccountNo());
				ErrorRecord inCorrectbalanceErrRecord = new ErrorRecord();
				inCorrectbalanceErrRecord.setAccountNumber(
						data.getAccountNo().concat(ApplicationConstant.OF_IN_CORRECT_END_BALANCE_RECORD));
				inCorrectbalanceErrRecord.setReferenceNo(
						data.getReferenceNo().toString().concat(ApplicationConstant.OF_IN_CORRECT_END_BALANCE_RECORD));
				inCorreectBalanceErrorRecords.add(inCorrectbalanceErrRecord);
			}
		}
		log.info("Second inCorreectBalanceErrorRecords size {}" , inCorreectBalanceErrorRecords.size());
		setResultMessage(response, duplicateErrorRecords, inCorreectBalanceErrorRecords);
		duplicateErrorRecords.addAll(inCorreectBalanceErrorRecords);
		log.info("duplicateErrorRecords size {}" , duplicateErrorRecords.size());
		response.setErrorRecords(duplicateErrorRecords);
		return response;
	}

	private void setResultMessage(StatementResponse response, List<ErrorRecord> duplicateErrorRecords,
			List<ErrorRecord> inCorreectBalanceErrorRecords) {
		if (duplicateErrorRecords.size() > 0 && inCorreectBalanceErrorRecords.size() > 0) {
			response.setResult(ApplicationConstant.DUPLICATE_REFERENCE_INCORRECT_END_BALANCE);
		} else if (duplicateErrorRecords.size() > 0) {
			response.setResult(ApplicationConstant.DUPLICATE_REFERENCE);
		} else if (inCorreectBalanceErrorRecords.size() > 0) {
			response.setResult(ApplicationConstant.INCORRECT_END_BALANCE);
		} else {
			response.setResult(ApplicationConstant.SUCCESSFUL);
		}
	}

	@Override
	public boolean checkDBIsAvailable() {
		// TODO Auto-generated method stub
		try {
			bankStatementDao.checkTransactionRecordExists(1);
			return true;
		} catch (Exception ex) {
			return false;
		}

	}

}
