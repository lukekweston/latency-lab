package com.hftbench.latency_lab.LowLatencyNoGc;

import java.util.List;

public class BenchmarkRunner {

    public BenchmarkResult run(BenchmarkConfig config) {
        int tradeCount = config.tradeCount > 0 ? config.tradeCount : 1000;
        List<Trade> trades = TradeGenerator.generate(tradeCount);
        BenchmarkMetrics metrics = new BenchmarkMetrics();
        TradeProcessor processor = create(config);

        metrics.start();
        if (config.enableBatching) {
            processor.processBatches(trades, config.batchSize, metrics);
        } else {
            processor.processTrades(trades, metrics);
        }
        metrics.stop();
        metrics.applySmoothing();

        BenchmarkResult result = new BenchmarkResult();
        result.latency = metrics.getLatencyMeasurements();
        result.throughput = metrics.getThroughputMeasurements();
        result.gcEvents = simulateGcEvents(config, result.latency.size());
        result.code = CodeGenerator.generate(config);
        result.explanation = ExplanationGenerator.generate(config);

        return result;
    }

    private List<Double> simulateGcEvents(BenchmarkConfig config, int size) {
        switch (config.gc) {
            case "Epsilon":
                return List.of();
            case "ZGC":
                return List.of(size * 0.4, size * 0.85);
            case "G1GC":
            default:
                return List.of(size * 0.2, size * 0.5, size * 0.8);
        }
    }

    TradeProcessor create(BenchmarkConfig config) {
        TradeProcessor processor = new NaiveTradeProcessor();

        // Apply config-driven wrappers (order matters for layering effects)
        if (config.useObjectPool) {
            processor = new PooledTradeProcessor(processor);
        }
        if (config.usePrimitives) {
            processor = new PrimitiveTradeProcessor(processor);
        }
        if ("offheap".equals(config.memoryAccess)) {
            processor = new OffHeapTradeProcessor(processor);
        }
        if (config.disableAllocations) {
            processor = new ReusedTradeProcessor(processor);
        }
        if (config.simulateLoad) {
            processor = new SimulatedLoadProcessor(processor);
        }

        return processor;
    }
}
