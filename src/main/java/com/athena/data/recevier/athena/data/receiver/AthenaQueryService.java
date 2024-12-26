package com.athena.data.recevier.athena.data.receiver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AthenaQueryService {

    private final AthenaClient athenaClient;
    private final String outputLocation = "s3://nf-aws-queries-bucket/";

    public AthenaQueryService(AthenaClient athenaClient) {
        this.athenaClient = athenaClient;
    }

    @Value("${aws.s3.output-bucket}")
    private String outputBucket;

    public String executeQuery(String query) {
        StartQueryExecutionRequest startQueryExecutionRequest = StartQueryExecutionRequest.builder()
                .queryString(query)
                .queryExecutionContext(QueryExecutionContext.builder()
                        .database("archive-db") // Replace with your database name
                        .build())
                .resultConfiguration(ResultConfiguration.builder()
                        .outputLocation(outputBucket)
                        .build())
                .build();

        StartQueryExecutionResponse response = athenaClient.startQueryExecution(startQueryExecutionRequest);
        return response.queryExecutionId();
    }

    public QueryExecutionState getQueryStatus(String queryExecutionId) {
        GetQueryExecutionRequest getQueryExecutionRequest = GetQueryExecutionRequest.builder()
                .queryExecutionId(queryExecutionId)
                .build();

        GetQueryExecutionResponse response = athenaClient.getQueryExecution(getQueryExecutionRequest);
        return response.queryExecution().status().state();
    }

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

