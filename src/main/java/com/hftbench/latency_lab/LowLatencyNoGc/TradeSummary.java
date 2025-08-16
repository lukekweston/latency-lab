package com.hftbench.latency_lab.LowLatencyNoGc;

// Stub for TradeSummary
public class TradeSummary {
    private double price;
    private int volume;
    private double notional;

    public TradeSummary(double price, int volume, double notional) {
        this.price = price;
        this.volume = volume;
        this.notional = notional;
    }

    public void init(double price, int volume, double notional) {
        this.price = price;
        this.volume = volume;
        this.notional = notional;
    }

    public void log() {
        if (notional > 0) System.nanoTime(); // Simulated side effect
    }
}