### **Amazon Athena Connection in Spring Boot**

---

#### **Overview**

This project demonstrates how to connect a Spring Boot application to Amazon Athena using the JDBC driver. Amazon Athena is a serverless query service that allows you to run SQL queries directly on data stored in Amazon S3.

The Spring Boot application executes Athena queries and retrieves results, enabling integration with your application for reporting, analytics, or data visualization purposes.

---

#### **Technologies Used**
- **Spring Boot**
- **AWS Athena JDBC Driver**
- **Amazon S3 (for query results)**
- **Java Persistence**

---

#### **Setup Instructions**

1. **Download the Athena JDBC Driver:**
    - Download the JDBC driver for Athena from the [Amazon Athena JDBC Driver Download Page](https://docs.aws.amazon.com/athena/latest/ug/connect-with-jdbc.html).
    - Place the downloaded `.jar` file in the `/src/main/resources/lib` folder (or an appropriate location).

2. **Add JDBC Driver to the Classpath:**
   Ensure that the Athena JDBC driver is included in the classpath when running your application.  
   Add the following configuration to your `pom.xml`:

   ```xml
   <dependency>
       <groupId>com.amazonaws.athena</groupId>
       <artifactId>jdbc</artifactId>
       <version>2.0.25</version> <!-- Update version as per your driver -->
       <scope>system</scope>
       <systemPath>${project.basedir}/src/main/resources/lib/AthenaJDBC.jar</systemPath>
   </dependency>
   ```
   or use 
   ```xml
   <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>sts</artifactId>
        <version>2.20.40</version>
   </dependency>
   ```
   without downloading Athena JDBC Driver

3. **Configure `application.properties`:**
   Add the necessary configuration for the Athena connection in your `application.properties` file:

   ```properties
   spring.datasource.url=jdbc:awsathena://AwsRegion=us-east-1;S3OutputLocation=s3://your-athena-output-bucket/
   spring.datasource.username=your-access-key
   spring.datasource.password=your-secret-key
   spring.datasource.driver-class-name=com.simba.athena.jdbc.Driver
   ```

    - **AwsRegion:** The AWS region where your Athena is located (e.g., `us-east-1`).
    - **S3OutputLocation:** The S3 bucket where query results will be stored.
    - **Username/Password:** AWS access key and secret (if required).

4. **Add Athena Connection Service:**

   ```java
   package com.athena.data.recevier.athena.data.receiver;

   import org.springframework.beans.factory.annotation.Value;
   import org.springframework.stereotype.Component;
   import org.springframework.stereotype.Service;
   import software.amazon.awssdk.services.athena.AthenaClient;
   import software.amazon.awssdk.services.athena.model.*;
   
   import java.util.List;
   import java.util.stream.Collectors;

   @Service
   public class AthenaService {

       private static final String JDBC_URL = "jdbc:awsathena://AwsRegion=us-east-1;S3OutputLocation=s3://your-athena-output-bucket/";

        public List<List<String>> getQueryResults(String queryExecutionId) {
            GetQueryResultsRequest resultsRequest = GetQueryResultsRequest.builder()
                .queryExecutionId(queryExecutionId)
                .build();

            GetQueryResultsResponse resultsResponse = athenaClient.getQueryResults(resultsRequest);

            return resultsResponse.resultSet().rows().stream()
                .map(row -> row.data().stream()
                        .map(Datum::varCharValue)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        }
   }
   ```

5. **Create a REST Controller:**
   Expose an API to execute Athena queries.

   ```java
   import org.springframework.web.bind.annotation.GetMapping;
   import org.springframework.web.bind.annotation.RequestParam;
   import org.springframework.web.bind.annotation.RestController;

   @RestController
   public class AthenaController {

       private final AthenaService athenaService;

       public AthenaController(AthenaService athenaService) {
           this.athenaService = athenaService;
       }

       @GetMapping("/loadTable")
        public String loadTableData(@RequestParam String tableName, Model model) {
            // Step 1: Execute query
            String query = "SELECT * FROM " + tableName + " LIMIT 10"; // Adjust query as needed
            String queryExecutionId = athenaQueryService.executeQuery(query);

            // Step 2: Wait for query to complete
            QueryExecutionState state;
            do {
                state = athenaQueryService.getQueryStatus(queryExecutionId);
                try {
                    Thread.sleep(1000); // Wait 1 second between checks
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (state == QueryExecutionState.RUNNING);

            // Step 3: Fetch results and pass to the view
            if (state == QueryExecutionState.SUCCEEDED) {
                List<List<String>> results = athenaQueryService.getQueryResults(queryExecutionId);
                model.addAttribute("data", results);
                return results.toString();
            } else {
                model.addAttribute("error", "Query failed: " + state);
                return "error"; // Render an error page
            }
        }
   }
   ```

6. **Run the Application:**
   Start the application and test the endpoint:
   ```bash
   ./mvnw spring-boot:run
   ```

   Test the API with a query:
   ```
   http://localhost:8080/loadTable?tableName=your_table_name
   ```

---

#### **Testing the Connection**
- Ensure that:
    - The Athena database is accessible.
    - The S3 bucket is correctly configured for storing query results.
    - The access key and secret key have the necessary IAM permissions for Athena and S3.

---