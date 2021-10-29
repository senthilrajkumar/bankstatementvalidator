package com.nl.rabobank.bankstatementvalidator.repository;

import org.springframework.data.repository.CrudRepository;

import com.nl.rabobank.bankstatementvalidator.domain.TransactionData;

public interface BankStatementRepository extends CrudRepository<TransactionData, Integer>{

	TransactionData findByReferenceNo(Integer referenceNo);
}
