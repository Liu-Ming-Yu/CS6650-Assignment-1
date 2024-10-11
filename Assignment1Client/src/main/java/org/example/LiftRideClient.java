package org.example;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LiftRideClient {
    public static void main(String[] args) throws InterruptedException {
        int totalEvents = 200000;
        int initialThreads = 32;
        int requestsPerThread = 1000;
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        BlockingQueue<LiftRide> queue = new LinkedBlockingQueue<>();
        String serverUrl = "http://ec2-54-89-220-25.compute-1.amazonaws.com:8080/untitled";

        // Start the Lift Ride Generator Thread
        Thread generatorThread = new Thread(new LiftRideGenerator(queue, totalEvents));
        generatorThread.start();

        long startTime = System.currentTimeMillis();

        // Create and start initial threads
        ExecutorService executor = Executors.newFixedThreadPool(initialThreads);

        for (int i = 0; i < initialThreads; i++) {
            executor.execute(new LiftRidePoster(queue, requestsPerThread, successCount, failureCount, serverUrl));
        }

        // Wait for any of the initial threads to complete
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.NANOSECONDS);

        // Calculate remaining requests
        int completedRequests = successCount.get() + failureCount.get();
        int remainingRequests = totalEvents - completedRequests;

        // Start subsequent threads
        if (remainingRequests > 0) {
            int subsequentThreads = 64; // Adjust as needed
            int requestsPerSubsequentThread = remainingRequests / subsequentThreads;
            ExecutorService subsequentExecutor = Executors.newFixedThreadPool(subsequentThreads);

            for (int i = 0; i < subsequentThreads; i++) {
                subsequentExecutor.execute(new LiftRidePoster(queue, requestsPerSubsequentThread, successCount, failureCount, serverUrl));
            }

            int leftoverRequests = remainingRequests % subsequentThreads;
            if (leftoverRequests > 0) {
                subsequentExecutor.execute(new LiftRidePoster(queue, leftoverRequests, successCount, failureCount, serverUrl));
            }

            subsequentExecutor.shutdown();
            subsequentExecutor.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.NANOSECONDS);
        }

        long endTime = System.currentTimeMillis();
        long wallTime = endTime - startTime;
        double throughput = (double) successCount.get() / (wallTime / 1000.0);

        System.out.println("Number of successful requests: " + successCount.get());
        System.out.println("Number of unsuccessful requests: " + failureCount.get());
        System.out.println("Total run time (ms): " + wallTime);
        System.out.println("Throughput (requests/second): " + throughput);
        // print out the client's configuration
        System.out.println("Total events: " + totalEvents);
        System.out.println("Initial threads: " + initialThreads);
        System.out.println("Subsequent threads: " + 64);
    }
}

