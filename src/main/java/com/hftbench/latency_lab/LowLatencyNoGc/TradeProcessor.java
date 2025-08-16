package com.hftbench.latency_lab.LowLatencyNoGc;


import java.util.List;

public interface TradeProcessor {
    void processTrades(List<Trade> trades, BenchmarkMetrics metrics);
    void processBatches(List<Trade> trades, int batchSize, BenchmarkMetrics metrics);
}
