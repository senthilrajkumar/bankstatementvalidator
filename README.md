Steps to follow to execute the application in your local:
1. Clone repo at your local
2. Run maven build
3. Run the SpringBootMainApplication(BankstatementvalidatorApplication)

4. Swagger Url: Please hit below url to access API
	http://localhost:8080/bank-app/swagger-ui.html

5. under "src/test/resources", all possible csv and xml files are placed using which Unit testing and Integration testing done. The same can be used for testing in swaager page

6. Two Main APIs are exposed. One is to upload CSV file data. Another one is to upload XML file data.

7. Two APIs are exposed for Health Check. one is for application health check and another one is for db health check(here h2 db is used to store data)

8. Using Jacoco , coverage report has been taken and published in the folder "Junit-Coverage-Report"

Framework Used:
1. SpringBoot Microservice
2. STS IDE used.
3. Layers Controller, Service , Processor and Dao and Spring Data Repository are included
4. Zulu JDK 11 and Junit 5 and h2 DB are used
5. Implementation Logic
   a. All incoming Transaction Records are verified whether duplicated within the Incoming request
   b. After that duplicate and distinct data are separated and checked against the data available in DB 
   c. Start Balance +/-Mutation = End Balance check is implemented
   d. Based on the above validations , Proper Object has been propogated to controller along with Response Entity.
6. JUnit and Integration Test cases are written
7. swagger has been used to document API and to test and view the APIs exposed.
8. schema.sql has been placed under "src/main/resources" to create table. 
