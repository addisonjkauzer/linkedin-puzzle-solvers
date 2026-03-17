package com.Utils;

import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

public class MetricsPublisher {

    private static final CloudWatchClient client = CloudWatchClient.create();

    public static void publishSolveTime(final String puzzleType, final long milliseconds) {
        try {
            client.putMetricData(PutMetricDataRequest.builder()
                    .namespace("LinkedInPuzzleSolvers")
                    .metricData(MetricDatum.builder()
                            .metricName("SolveExecutionTime")
                            .unit(StandardUnit.MILLISECONDS)
                            .value((double) milliseconds)
                            .dimensions(Dimension.builder()
                                    .name("PuzzleType")
                                    .value(puzzleType)
                                    .build())
                            .build())
                    .build());
        } catch (Exception e) {
            System.err.println("Failed to publish CloudWatch metric for " + puzzleType + ": " + e.getMessage());
        }
    }
}
