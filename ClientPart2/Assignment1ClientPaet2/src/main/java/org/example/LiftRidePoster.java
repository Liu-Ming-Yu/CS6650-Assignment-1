package org.example;

import java.util.concurrent.BlockingQueue;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LiftRidePoster implements Runnable {
    private final BlockingQueue<LiftRide> queue;
    private final int numRequests;
    private final AtomicInteger successCount;
    private final AtomicInteger failureCount;
    private final String serverUrl;
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final ConcurrentLinkedQueue<RequestRecord> requestRecords;

    public LiftRidePoster(BlockingQueue<LiftRide> queue, int numRequests,
                          AtomicInteger successCount, AtomicInteger failureCount,
                          String serverUrl, ConcurrentLinkedQueue<RequestRecord> requestRecords) {
        this.queue = queue;
        this.numRequests = numRequests;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.serverUrl = serverUrl;
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.requestRecords = requestRecords;
    }

    @Override
    public void run() {
        for (int i = 0; i < numRequests; i++) {
            LiftRide liftRide = queue.poll();
            if (liftRide != null) {
                sendPostRequest(liftRide);
            } else {
                // No more lift rides to send
                break;
            }
        }
    }

    private void sendPostRequest(LiftRide liftRide) {
        int attempts = 0;
        boolean success = false;
        while (attempts < 5 && !success) {
            attempts++;
            long startTime = System.currentTimeMillis();
            try {
                String requestBody = mapper.writeValueAsString(liftRide);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(serverUrl + "/skiers"))
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .header("Content-Type", "application/json")
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                long endTime = System.currentTimeMillis();
                long latency = endTime - startTime;
                int statusCode = response.statusCode();

                // Record the request
                RequestRecord record = new RequestRecord(startTime, "POST", latency, statusCode);
                requestRecords.add(record);

                if (statusCode == 201) {
                    successCount.incrementAndGet();
                    success = true;
                } else if (statusCode >= 500 || statusCode >= 400) {
                    // Retry on server or client error
                    Thread.sleep(100); // Brief pause before retrying
                } else {
                    // Other non-retryable errors
                    failureCount.incrementAndGet();
                    success = true; // Stop retrying
                }
            } catch (Exception e) {
                long endTime = System.currentTimeMillis();
                long latency = endTime - startTime;

                // Record the request with exception (response code 0)
                RequestRecord record = new RequestRecord(startTime, "POST", latency, 0);
                requestRecords.add(record);

                // Handle exceptions (e.g., network errors)
                try {
                    Thread.sleep(100); // Brief pause before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        if (!success) {
            failureCount.incrementAndGet();
        }
    }
}

