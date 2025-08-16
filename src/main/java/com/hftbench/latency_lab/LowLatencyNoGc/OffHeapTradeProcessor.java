package com.hftbench.latency_lab.LowLatencyNoGc;

import java.nio.ByteBuffer;
import java.util.List;

// Simulates off-heap memory access using ByteBuffer
class OffHeapTradeProcessor implements TradeProcessor {
    private final TradeProcessor delegate;
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(8);

    public OffHeapTradeProcessor(TradeProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void processTrades(List<Trade> trades, BenchmarkMetrics metrics) {
        delegate.processTrades(trades, metrics);
        for (Trade trade : trades) {
            buffer.putDouble(0, trade.getPrice());
        }
    }

    @Override
    public void processBatches(List<Trade> trades, int batchSize, BenchmarkMetrics metrics) {
        delegate.processBatches(trades, batchSize, metrics);
        for (Trade trade : trades) {
            buffer.putDouble(0, trade.getPrice());
        }
    }
}
