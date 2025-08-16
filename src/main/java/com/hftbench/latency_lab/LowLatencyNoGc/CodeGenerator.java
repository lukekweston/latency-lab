package com.hftbench.latency_lab.LowLatencyNoGc;

public class CodeGenerator {

    public static String generate(BenchmarkConfig config) {
        StringBuilder code = new StringBuilder();

        // Setup section
        if ("threadpool".equals(config.threading)) {
            code.append("private final ExecutorService executor = Executors.newFixedThreadPool(4);\n");
        }
        if (config.useObjectPool) {
            code.append("private final TradeSummaryPool summaryPool = new TradeSummaryPool();\n");
        }
        if ("offheap".equals(config.memoryAccess)) {
            code.append("private final ByteBuffer buffer = ByteBuffer.allocateDirect(8192);\n");
        }

        code.append("\npublic String processTrades(List<Trade> trades) {\n");

        if (config.enableBatching) {
            code.append("    List<List<Trade>> batches = batchTrades(trades, ").append(config.batchSize).append(");\n");
            code.append("    for (List<Trade> batch : batches) {\n");
        }

        String loopPrefix = config.enableBatching ? "        " : "    ";
        if ("threadpool".equals(config.threading)) {
            code.append(loopPrefix).append("executor.submit(() -> {\n");
            loopPrefix += "    ";
        }

        code.append(loopPrefix).append("StringBuilder result = new StringBuilder();\n");

        code.append(loopPrefix).append("for (Trade trade : ");
        code.append(config.enableBatching ? "batch" : "trades").append(") {\n");

        String indent = loopPrefix + "    ";

        // Primitive or boxed
        if (config.usePrimitives) {
            code.append(indent).append("double price = trade.getPrice();\n");
            code.append(indent).append("int volume = trade.getVolume();\n");
        } else {
            code.append(indent).append("Double price = trade.getPrice();\n");
            code.append(indent).append("Integer volume = trade.getVolume();\n");
        }

        code.append(indent).append("double notional = price * volume;\n");

        if (config.disableAllocations) {
            code.append(indent).append("// Reusing TradeSummary object (allocations disabled)\n");
            code.append(indent).append("TradeSummary summary = summaryCache;\n");
            code.append(indent).append("summary.update(price, volume, notional);\n");
        } else if (config.useObjectPool) {
            code.append(indent).append("TradeSummary summary = summaryPool.borrow();\n");
            code.append(indent).append("summary.init(price, volume, notional);\n");
        } else {
            code.append(indent).append("TradeSummary summary = new TradeSummary(price, volume, notional);\n");
        }

        if ("offheap".equals(config.memoryAccess)) {
            code.append(indent).append("// Simulate off-heap memory write\n");
            code.append(indent).append("buffer.putDouble(0, notional);\n");
        }

        if (config.simulateLoad) {
            code.append(indent).append("// Simulated load\n");
            code.append(indent).append("Thread.sleep(1);\n");
        }

        code.append(indent).append("summary.log();\n");
        code.append(indent).append("result.append(\"Trade Notional: \").append(notional).append(\"\\n\");\n");

        if (config.useObjectPool && !config.disableAllocations) {
            code.append(indent).append("summaryPool.release(summary);\n");
        }

        code.append(loopPrefix).append("}\n");

        if ("threadpool".equals(config.threading)) {
            code.append(config.enableBatching ? "        " : "    ").append("});\n");
        }

        if (config.enableBatching) {
            code.append("    }\n");
        }

        code.append("    return result.toString();\n");
        code.append("}\n");

        if (config.enableBatching) {
            code.append("\nprivate List<List<Trade>> batchTrades(List<Trade> trades, int size) {\n");
            code.append("    List<List<Trade>> batches = new ArrayList<>();\n");
            code.append("    for (int i = 0; i < trades.size(); i += size) {\n");
            code.append("        batches.add(trades.subList(i, Math.min(i + size, trades.size())));\n");
            code.append("    }\n");
            code.append("    return batches;\n");
            code.append("}\n");
        }

        return code.toString();
    }
}