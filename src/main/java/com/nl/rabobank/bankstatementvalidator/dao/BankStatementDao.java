package com.nl.rabobank.bankstatementvalidator.dao;

import com.nl.rabobank.bankstatementvalidator.domain.TransactionData;

public interface BankStatementDao {

    void persistTransactionRecords(TransactionData record);

    boolean checkTransactionRecordExists(Integer referenceNo);

}
