package com.hftbench.latency_lab.LowLatencyNoGc;

import java.util.List;

// Base trade processor
public class NaiveTradeProcessor implements TradeProcessor {
    @Override
    public void processTrades(List<Trade> trades, BenchmarkMetrics metrics) {
        StringBuilder result = new StringBuilder();
        for (Trade trade : trades) {
            Double price = trade.getPrice();
            Integer volume = trade.getVolume();
            double notional = price * volume;
            TradeSummary summary = new TradeSummary(price, volume, notional);
            summary.log();
            result.append("Trade Notional: ").append(notional).append("\n");
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