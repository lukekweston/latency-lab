package com.hftbench.latency_lab.LowLatencyNoGc;

import java.util.List;

public class BenchmarkResult {
    public List<Double> latency;
    public List<Double> throughput;
    public List<Double> gcEvents;
    public String code;
    public List<String> explanation;
}