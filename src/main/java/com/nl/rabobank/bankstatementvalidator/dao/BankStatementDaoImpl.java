package com.nl.rabobank.bankstatementvalidator.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.nl.rabobank.bankstatementvalidator.domain.TransactionData;
import com.nl.rabobank.bankstatementvalidator.repository.BankStatementRepository;

@Repository("bankStatementDao")
public class BankStatementDaoImpl implements BankStatementDao {

	private static final Logger log = LoggerFactory.getLogger(BankStatementDaoImpl.class);

	@Autowired
	private BankStatementRepository repository;

	@Override
	public void persistTransactionRecords(TransactionData record) {
		// TODO Auto-generated method stub
		log.info("persistTransactionRecords before invoking db");
		repository.save(record);
		log.info("persistTransactionRecords after db call");
	}

	@Override
	public boolean checkTransactionRecordExists(Integer referenceNo) {
		// TODO Auto-generated method stub
		if (repository.findByReferenceNo(referenceNo) != null) {
			log.info("Record Exists in DB for referenceNo {}" , referenceNo);
			return true;
		} else {
			log.info("Record does not exist in DB for referenceNo {}" , referenceNo);
			return false;
		}
	}

}
