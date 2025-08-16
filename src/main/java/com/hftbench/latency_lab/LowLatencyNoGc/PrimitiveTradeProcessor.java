package com.hftbench.latency_lab.LowLatencyNoGc;

import java.util.List;

public class PrimitiveTradeProcessor implements TradeProcessor {
    private final TradeProcessor delegate;

    public PrimitiveTradeProcessor(TradeProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void processTrades(List<Trade> trades, BenchmarkMetrics metrics) {
        for (Trade trade : trades) {
            double price = trade.getPrice(); // unboxed directly
            int volume = trade.getVolume();  // unboxed directly
            double notional = price * volume;
            TradeSummary summary = new TradeSummary(price, volume, notional);
            summary.log();
            metrics.recordOp();
        }
    }

    @Override
    public void processBatches(List<Trade> trades, int batchSize, BenchmarkMetrics metrics) {
        for (int i = 0; i < trades.size(); i += batchSize) {
            List<Trade> batch = trades.subList(i, Math.min(i + batchSize, trades.size()));
            processTrades(batch, metrics);
        }
    }
}
