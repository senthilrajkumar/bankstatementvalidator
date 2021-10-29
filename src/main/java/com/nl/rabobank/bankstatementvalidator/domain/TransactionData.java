package com.nl.rabobank.bankstatementvalidator.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nl.rabobank.bankstatementvalidator.constant.ApplicationConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Bank_Transaction")
public class TransactionData {

    @NotNull(message = ApplicationConstant.TRANSACTION_REFERENCE_IS_MANDATORY)
    @JsonProperty("Transaction reference")
    @Id
    private Long referenceNo;

    @NotNull(message = ApplicationConstant.ACCOUNT_NUMBER_IS_MANDATORY)
    @Pattern(regexp = ApplicationConstant.REGEX_IBAN, message = ApplicationConstant.ACCOUNT_NUMBER_FORMAT)
    @JsonProperty("Account number")
    private String accountNo;

    @NotNull(message = ApplicationConstant.START_BALANCE_IS_MANDATORY)
    @JsonProperty("Start Balance")
    private BigDecimal startBalance;


    @NotNull(message = ApplicationConstant.MUTATION_IS_MANDATORY)
    @JsonProperty("Mutation")
    private BigDecimal mutation;

    @JsonProperty("Description")
    private String description;

    @NotNull(message = ApplicationConstant.END_BALANCE_IS_MANDATORY)
    @JsonProperty("End Balance")
    private BigDecimal endBalance;


}
