package com.hftbench.latency_lab.LowLatencyNoGc;

import java.util.List;

// Simulates allocation-free reuse of a Trade instance
class ReusedTradeProcessor implements TradeProcessor {
    private final TradeProcessor delegate;
    private final Trade reusableTrade = new Trade(0.0, 0);

    public ReusedTradeProcessor(TradeProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void processTrades(List<Trade> trades, BenchmarkMetrics metrics) {
        for (Trade trade : trades) {
            reusableTrade.setPrice(trade.getPrice());
            reusableTrade.setVolume(trade.getVolume());
            delegate.processTrades(List.of(reusableTrade), metrics);
        }
    }

    @Override
    public void processBatches(List<Trade> trades, int batchSize, BenchmarkMetrics metrics) {
        for (int i = 0; i < trades.size(); i += batchSize) {
            List<Trade> batch = trades.subList(i, Math.min(i + batchSize, trades.size()));
            for (Trade trade : batch) {
                reusableTrade.setPrice(trade.getPrice());
                reusableTrade.setVolume(trade.getVolume());
                delegate.processTrades(List.of(reusableTrade), metrics);
            }
        }
    }
}
