package com.nl.rabobank.bankstatementvalidator.constant;

/**
 * @author ADMIN
 *
 */
public final class ApplicationConstant {

	public static final String V1_RECORD_VERIFICATION = "/v1/record-verification";
	public static final String BANK_STATEMENT = "/bank-statement";

	public static final String APPLICATION_IS_UP_AND_RUNNING = "Application is Up and Running";

	public static final String DB_IS_UP_AND_RUNNING = "DB is Up and Running";

	public static final String DB_IS_DOWN = "DB is Not Available";

	public static final String SUCCESSFUL = "SUCCESSFUL";
	public static final String INCORRECT_END_BALANCE = "INCORRECT_END_BALANCE";
	public static final String DUPLICATE_REFERENCE = "DUPLICATE_REFERENCE";
	public static final String DUPLICATE_REFERENCE_INCORRECT_END_BALANCE = "DUPLICATE_REFERENCE_INCORRECT_END_BALANCE";
	public static final String OF_IN_CORRECT_END_BALANCE_RECORD = " of inCorrectEndBalance record";
	public static final String OF_DUPLICATE_RECORD = " of duplicate record";
	
	public static final String ACCOUNT_NUMBER_FORMAT = "Account Number should be in the right format";

	public static final String REGEX_IBAN = "^[a-zA-Z]{2}[0-9]{2}\\s?[a-zA-Z0-9]{4}\\s?[0-9]{4}\\s?[0-9]{3}([a-zA-Z0-9]\\s?[a-zA-Z0-9]{0,4}\\s?[a-zA-Z0-9]{0,4}\\s?[a-zA-Z0-9]{0,4}\\s?[a-zA-Z0-9]{0,3})?$";

	public static final String END_BALANCE_IS_MANDATORY = "End Balance is mandatory";

	public static final String MUTATION_IS_MANDATORY = "Mutation is mandatory";

	public static final String START_BALANCE_IS_MANDATORY = "Start Balance is mandatory";

	public static final String ACCOUNT_NUMBER_IS_MANDATORY = "Account number is mandatory";

	public static final String TRANSACTION_REFERENCE_IS_MANDATORY = "Transaction reference is mandatory";
	
	public static final String TEXT_XML_TYPE = "text/xml";
	
	public static final String APP_XML_TYPE = "application/xml";

	public static final String CSV_TYPE = "application/vnd.ms-excel";
	
	public static final String CSV_FILE = "Please upload a csv file!";
	
	public static final String XML_FILE = "Please upload a xml file!";

}
