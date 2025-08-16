package com.hftbench.latency_lab.LowLatencyNoGc;

import java.util.ArrayDeque;
import java.util.Queue;

public class TradeSummaryPool implements ObjectPool<TradeSummary> {
    private final Queue<TradeSummary> pool = new ArrayDeque<>();

    @Override
    public TradeSummary borrow() {
        return pool.poll();
    }

    @Override
    public void release(TradeSummary obj) {
        pool.offer(obj);
    }
}
