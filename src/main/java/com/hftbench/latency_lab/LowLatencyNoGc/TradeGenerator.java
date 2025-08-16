package com.hftbench.latency_lab.LowLatencyNoGc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// TradeGenerator generates dummy trades
public class TradeGenerator {
    public static List<Trade> generate(int count) {
        List<Trade> trades = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            trades.add(new Trade(50.0 + rand.nextDouble() * 10, rand.nextInt(1000)));
        }
        return trades;
    }
}