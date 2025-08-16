package com.hftbench.latency_lab.LowLatencyNoGc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// BenchmarkMetrics for tracking time
public class BenchmarkMetrics {
    private final List<Double> latency = new ArrayList<>();
    private final List<Long> timestamps = new ArrayList<>();
    private final List<Double> throughput = new ArrayList<>();
    private int tradeCounter = 0;
    private long startTime;
    private long lastOpTime;

    public void start() {
        startTime = System.nanoTime();
        lastOpTime = startTime;
    }

    public void recordOp() {
        long now = System.nanoTime();
        latency.add((now - lastOpTime) / 1_000_000.0); // ms
        lastOpTime = now;

        tradeCounter++;
        if (tradeCounter % 100 == 0) {
            double elapsedSec = (now - startTime) / 1_000_000_000.0;
            double opsPerSec = tradeCounter / elapsedSec;
            throughput.add(opsPerSec);
        }
    }

    public void stop() {
        // Placeholder for any post-processing if needed
    }

    public List<Double> getLatencyMeasurements() {
        return latency;
    }

    public List<Double> getThroughputMeasurements() {
        return throughput;
    }


    public void applySmoothing() {
        if (latency.size() < 5) return;

        List<Double> smoothed = new ArrayList<>();
        for (int i = 2; i < latency.size() - 2; i++) {
            double avg = (
                    latency.get(i - 2) + latency.get(i - 1) +
                            latency.get(i) + latency.get(i + 1) +
                            latency.get(i + 2)
            ) / 5.0;
            smoothed.add(avg);
        }
        latency.clear();
        latency.addAll(smoothed);
    }

    public void clampOutliers(double percentile) {
        if (latency.isEmpty()) return;
        List<Double> sorted = new ArrayList<>(latency);
        Collections.sort(sorted);
        double max = sorted.get((int) (percentile / 100.0 * sorted.size()));

        for (int i = 0; i < latency.size(); i++) {
            if (latency.get(i) > max) {
                latency.set(i, max);
            }
        }
    }

}
