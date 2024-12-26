package com.athena.data.recevier.athena.data.receiver;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.athena.model.QueryExecutionState;
import software.amazon.awssdk.services.athena.model.Row;

import java.util.List;

@RestController
public class AthenaController {

    private final AthenaQueryService athenaQueryService;

    public AthenaController(AthenaQueryService athenaQueryService) {
        this.athenaQueryService = athenaQueryService;
    }

    @GetMapping("/executeQuery")
    public String executeQuery(@RequestParam String query) {
        return athenaQueryService.executeQuery(query);
    }

    @GetMapping("/loadTable")
    public String loadTableData(@RequestParam String tableName, Model model) {
        // Step 1: Execute query
        String query = "SELECT * FROM " + tableName + " LIMIT 10"; // Adjust query as needed
        String queryExecutionId = athenaQueryService.executeQuery(query);

        // Step 2: Wait for query to complete (simple retry logic)
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

