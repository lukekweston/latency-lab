package com.hftbench.latency_lab.LowLatencyNoGc;

import java.util.*;
import java.util.concurrent.*;

// Dummy Trade class
public class Trade {
    private Double price;
    private Integer volume;

    public Trade(Double price, Integer volume) {
        this.price = price;
        this.volume = volume;
    }

    public Double getPrice() { return price; }
    public Integer getVolume() { return volume; }
    public void setPrice(Double price) { this.price = price; }
    public void setVolume(Integer volume) { this.volume = volume; }
}