package com.nl.rabobank.bankstatementvalidator.dao;

import com.nl.rabobank.bankstatementvalidator.domain.TransactionData;
import com.nl.rabobank.bankstatementvalidator.repository.BankStatementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("bankStatementDao")
@Slf4j
public class BankStatementDaoImpl implements BankStatementDao {

    @Autowired
    private BankStatementRepository repository;

    @Override
    public void persistTransactionRecords(TransactionData record) {
        log.info("persistTransactionRecords before invoking db");
        repository.save(record);
        log.info("persistTransactionRecords after db call");
    }

    @Override
    public boolean checkTransactionRecordExists(Integer referenceNo) {
        if (repository.findByReferenceNo(referenceNo) != null) {
            log.info("Record Exists in DB for referenceNo {}", referenceNo);
            return true;
        } else {
            log.info("Record does not exist in DB for referenceNo {}", referenceNo);
            return false;
        }
    }

}
