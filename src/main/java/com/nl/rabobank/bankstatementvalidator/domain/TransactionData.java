package com.nl.rabobank.bankstatementvalidator.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nl.rabobank.bankstatementvalidator.constant.ApplicationConstant;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@Entity(name = "Bank_Transaction")
public class TransactionData {

    @NotNull(message = ApplicationConstant.TRANSACTION_REFERENCE_IS_MANDATORY)
    @JsonProperty("Transaction reference")
    @Id
    private Integer referenceNo;

    @NotNull(message = ApplicationConstant.ACCOUNT_NUMBER_IS_MANDATORY)
    @Pattern(regexp = ApplicationConstant.REGEX_IBAN, message = ApplicationConstant.ACCOUNT_NUMBER_FORMAT)
    @JsonProperty("Account number")
    private String accountNo;

    @NotNull(message = ApplicationConstant.START_BALANCE_IS_MANDATORY)
    @JsonProperty("Start Balance")
    private Double startBalance;


    @NotNull(message = ApplicationConstant.MUTATION_IS_MANDATORY)
    @JsonProperty("Mutation")
    private String mutation;

    @JsonProperty("Description")
    private String description;

    @NotNull(message = ApplicationConstant.END_BALANCE_IS_MANDATORY)
    @JsonProperty("End Balance")
    private Double endBalance;


}
