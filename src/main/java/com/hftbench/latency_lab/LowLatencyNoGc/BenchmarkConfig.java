package com.hftbench.latency_lab.LowLatencyNoGc;

public class BenchmarkConfig {
    // GC & Memory
    public String gc; // e.g., "G1GC", "ZGC", "Epsilon"
    public boolean useObjectPool;
    public boolean usePrimitives;
    public boolean disableAllocations;
    public boolean preTouchMemory;
    public String memoryAccess; // e.g., "onheap", "direct", "offheap"

    // Concurrency
    public String threading; // "single" or "threadpool"
    public boolean pinThreads;

    // Throughput
    public boolean enableBatching;
    public int batchSize;

    // Advanced
    public boolean escapeAnalysisDisabled;
    public boolean simulateLoad;

    public int tradeCount = 10000;
}
