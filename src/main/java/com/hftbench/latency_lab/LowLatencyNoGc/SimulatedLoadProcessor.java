package com.hftbench.latency_lab.LowLatencyNoGc;

import java.util.List;

// Adds CPU load simulation to each op
class SimulatedLoadProcessor implements TradeProcessor {
    private final TradeProcessor delegate;

    public SimulatedLoadProcessor(TradeProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void processTrades(List<Trade> trades, BenchmarkMetrics metrics) {
        for (Trade trade : trades) {
            spinCpu();
            delegate.processTrades(List.of(trade), metrics);
        }
    }

    @Override
    public void processBatches(List<Trade> trades, int batchSize, BenchmarkMetrics metrics) {
        for (int i = 0; i < trades.size(); i += batchSize) {
            List<Trade> batch = trades.subList(i, Math.min(i + batchSize, trades.size()));
            for (Trade trade : batch) {
                spinCpu();
                delegate.processTrades(List.of(trade), metrics);
            }
        }
    }

    private void spinCpu() {
        long start = System.nanoTime();
        while (System.nanoTime() - start < 20000) {
            // spin ~20 microseconds
        }
    }
}

