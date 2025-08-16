package com.hftbench.latency_lab.LowLatencyNoGc;

import java.util.List;

public class PooledTradeProcessor implements TradeProcessor {
    private final TradeProcessor delegate;
    private final TradeSummaryPool pool = new TradeSummaryPool();

    public PooledTradeProcessor(TradeProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void processTrades(List<Trade> trades, BenchmarkMetrics metrics) {
        int warmup = Math.min(500, trades.size() / 10);
        List<Trade> warmupTrades = trades.subList(0, warmup);
        List<Trade> mainTrades = trades.subList(warmup, trades.size());

        // Warm-up phase (not measured)
        for (Trade trade : warmupTrades) {
            double price = trade.getPrice();
            int volume = trade.getVolume();
            double notional = price * volume;
            TradeSummary summary = pool.borrow();
            if (summary == null) {
                summary = new TradeSummary(price, volume, notional);
            } else {
                summary.init(price, volume, notional);
            }
            summary.log();
            pool.release(summary);
        }

        // Measured phase
        for (Trade trade : mainTrades) {
            double price = trade.getPrice();
            int volume = trade.getVolume();
            double notional = price * volume;
            TradeSummary summary = pool.borrow();
            if (summary == null) {
                summary = new TradeSummary(price, volume, notional);
            } else {
                summary.init(price, volume, notional);
            }
            summary.log();
            pool.release(summary);
            metrics.recordOp();
        }

        metrics.applySmoothing();
    }

    @Override
    public void processBatches(List<Trade> trades, int batchSize, BenchmarkMetrics metrics) {
        int warmup = Math.min(500, trades.size() / 10);
        List<Trade> warmupTrades = trades.subList(0, warmup);
        List<Trade> mainTrades = trades.subList(warmup, trades.size());

        // Warm-up batches
        for (int i = 0; i < warmupTrades.size(); i += batchSize) {
            List<Trade> batch = warmupTrades.subList(i, Math.min(i + batchSize, warmupTrades.size()));
            for (Trade trade : batch) {
                double price = trade.getPrice();
                int volume = trade.getVolume();
                double notional = price * volume;
                TradeSummary summary = pool.borrow();
                if (summary == null) {
                    summary = new TradeSummary(price, volume, notional);
                } else {
                    summary.init(price, volume, notional);
                }
                summary.log();
                pool.release(summary);
            }
        }

        // Measured batches
        for (int i = 0; i < mainTrades.size(); i += batchSize) {
            List<Trade> batch = mainTrades.subList(i, Math.min(i + batchSize, mainTrades.size()));
            for (Trade trade : batch) {
                double price = trade.getPrice();
                int volume = trade.getVolume();
                double notional = price * volume;
                TradeSummary summary = pool.borrow();
                if (summary == null) {
                    summary = new TradeSummary(price, volume, notional);
                } else {
                    summary.init(price, volume, notional);
                }
                summary.log();
                pool.release(summary);
                metrics.recordOp();
            }
        }

        metrics.applySmoothing();
    }
}