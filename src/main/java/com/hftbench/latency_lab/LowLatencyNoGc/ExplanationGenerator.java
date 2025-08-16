package com.hftbench.latency_lab.LowLatencyNoGc;

import java.util.ArrayList;
import java.util.List;

public class ExplanationGenerator {

    public static List<String> generate(BenchmarkConfig config) {
        List<String> explanation = new ArrayList<>();

        switch (config.gc) {
            case "Epsilon":
                explanation.add("- **GC Strategy: Epsilon** — No garbage collection occurs. Useful for benchmarking raw allocation overhead but risks OOM.");
                break;
            case "ZGC":
                explanation.add("- **GC Strategy: ZGC** — A low-pause, concurrent collector ideal for large heaps and latency-sensitive workloads.");
                break;
            case "G1GC":
            default:
                explanation.add("- **GC Strategy: G1GC** — Balanced throughput/latency collector with predictable minor GCs.");
                break;
        }

        if (config.useObjectPool) {
            explanation.add("- **Object Pooling Enabled** — Reuses TradeSummary instances to avoid allocation and reduce GC pressure.");
        }

        if (config.usePrimitives) {
            explanation.add("- **Primitives Used** — Avoids boxing overhead by using `double` and `int` directly.");
        }

        if (config.disableAllocations) {
            explanation.add("- **Allocations Disabled** — All memory reuse is forced to avoid allocation entirely.");
        }

        if (config.preTouchMemory) {
            explanation.add("- **Memory Pre-touch Enabled** — Simulates early memory access to reduce runtime paging latency.");
        }

        switch (config.memoryAccess) {
            case "direct":
                explanation.add("- **Direct Memory Access** — Uses direct byte buffers to bypass heap GC.");
                break;
            case "offheap":
                explanation.add("- **Off-Heap Access** — Simulates memory access outside the Java heap.");
                break;
            case "onheap":
            default:
                explanation.add("- **Heap Memory Access** — Uses standard heap allocations with normal GC cost.");
                break;
        }

        if ("threadpool".equals(config.threading)) {
            explanation.add("- **Thread Pooling Enabled** — Tasks are processed in parallel via executor threads.");
        } else {
            explanation.add("- **Single-threaded Execution** — Simple and predictable, but lower throughput.");
        }

        if (config.pinThreads) {
            explanation.add("- **Thread Pinning Simulated** — Threads are simulated to stick to fixed cores (affects cache locality).");
        }

        if (config.enableBatching) {
            explanation.add("- **Batching Enabled** — Trades are grouped and processed in fixed-size batches.");
            explanation.add("  - **Batch Size:** " + config.batchSize);
        }

        if (config.escapeAnalysisDisabled) {
            explanation.add("- **Escape Analysis Disabled (Simulated)** — JVM optimization for allocation elision is turned off.");
        }

        if (config.simulateLoad) {
            explanation.add("- **Simulated Load** — Background load (e.g. sleep or busy-loop) introduces latency noise.");
        }

        return explanation;
    }
}