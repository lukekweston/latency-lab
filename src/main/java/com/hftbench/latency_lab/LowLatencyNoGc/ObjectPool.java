package com.hftbench.latency_lab.LowLatencyNoGc;

public interface ObjectPool<T> {
    T borrow();
    void release(T obj);
}
