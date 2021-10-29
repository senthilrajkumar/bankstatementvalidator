package com.nl.rabobank.bankstatementvalidator.repository;

import com.nl.rabobank.bankstatementvalidator.domain.TransactionData;
import org.springframework.data.repository.CrudRepository;

public interface BankStatementRepository extends CrudRepository<TransactionData, Integer> {

    TransactionData findByReferenceNo(Integer referenceNo);

}
