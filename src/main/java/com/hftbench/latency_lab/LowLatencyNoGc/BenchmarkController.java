package com.hftbench.latency_lab.LowLatencyNoGc;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class BenchmarkController {

    @PostMapping("/run-benchmark")
    public BenchmarkResult runBenchmark(@RequestBody BenchmarkConfig config) {
        BenchmarkResult result = new BenchmarkResult();

        result.latency = simulateLatency(config);
        result.throughput = simulateThroughput(config);
        int size = result.latency.size();
        result.gcEvents = simulateGcEvents(config, size);
        return result;
    }

    @PostMapping("/generate-code-snippet")
    public String generateCode(@RequestBody BenchmarkConfig config) {
        return generateCodeSnippet(config);
    }

    @PostMapping("/generate-explanation")
    public List<String> generateExplanationText(@RequestBody BenchmarkConfig config) {
        return generateExplanation(config);
    }

    private List<Double> simulateGcEvents(BenchmarkConfig config, int latencySize) {
        switch (config.gc) {
            case "Epsilon": // No GC
                return List.of();

            case "ZGC": // Infrequent and light GCs
                return List.of(
                        latencySize * 0.40,
                        latencySize * 0.85
                );

            case "G1GC": // Frequent minor GCs
            default:
                return List.of(
                        latencySize * 0.20,
                        latencySize * 0.50,
                        latencySize * 0.80
                );
        }
    }


    private List<Double> simulateLatency(BenchmarkConfig config) {
        List<Double> values = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < 100; i++) {
            double base = 1.2;

            if (!config.usePrimitives) base += 0.8;
            if (!config.useObjectPool) base += 1.0;
            if ("threadpool".equals(config.threading)) base -= 0.4;

            // Simulate GC spikes
            if (i == 20 || i == 50 || i == 80) {
                values.add(base + 10 + rand.nextDouble());
            } else {
                values.add(base + rand.nextDouble() * 0.2);
            }
        }
        return values;
    }

    private List<Double> simulateThroughput(BenchmarkConfig config) {
        List<Double> values = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < 100; i++) {
            double base = 10000;

            if (config.usePrimitives) base += 2000;
            if (config.useObjectPool) base += 3000;
            if ("threadpool".equals(config.threading)) base += 5000;

            // GC slowdown simulation
            if (i == 20 || i == 50 || i == 80) {
                values.add(base - 4000 + rand.nextDouble() * 100);
            } else {
                values.add(base + rand.nextDouble() * 100);
            }
        }
        return values;
    }

    public String generateCodeSnippet(BenchmarkConfig config) {
        StringBuilder code = new StringBuilder();

        // Setup section
        if (config.threading.equals("threadpool")) {
            code.append("private final ExecutorService executor = Executors.newFixedThreadPool(4);\n");
        }
        if (config.useObjectPool) {
            code.append("private final TradePool tradePool = new TradePool();\n");
        }
        if ("offheap".equals(config.memoryAccess)) {
            code.append("private final ByteBuffer buffer = ByteBuffer.allocateDirect(4096);\n");
        }
        if (config.enableBatching) {
            code.append("\nprivate List<List<Order>> batchOrders(List<Order> orders, int batchSize) {\n")
                    .append("    List<List<Order>> batches = new ArrayList<>();\n")
                    .append("    for (int i = 0; i < orders.size(); i += batchSize) {\n")
                    .append("        batches.add(orders.subList(i, Math.min(i + batchSize, orders.size())));\n")
                    .append("    }\n")
                    .append("    return batches;\n")
                    .append("}\n");
        }

        code.append("\n");

        // Method signature
        code.append("public void processOrders(List<Order> orders) {\n");

        // Batching logic
        if (config.enableBatching) {
            code.append("    List<List<Order>> batches = batchOrders(orders, ").append(config.batchSize).append(");\n");
            code.append("    for (List<Order> batch : batches) {\n");
            if (config.threading.equals("threadpool")) {
                code.append("        executor.submit(() -> {\n")
                        .append("            for (Order order : batch) {\n");
            } else {
                code.append("        for (Order order : batch) {\n");
            }
        } else if (config.threading.equals("threadpool")) {
            code.append("    for (Order order : orders) {\n")
                    .append("        executor.submit(() -> {\n");
        } else {
            code.append("    for (Order order : orders) {\n");
        }

        // Inside loop: Trade creation
        String indent = (config.enableBatching && config.threading.equals("threadpool")) ? "                "
                : (config.enableBatching || config.threading.equals("threadpool")) ? "            " : "        ";

        if (config.useObjectPool) {
            code.append(indent).append("Trade trade = tradePool.borrow();\n");
        } else {
            code.append(indent).append("Trade trade = new Trade();\n");
        }

        if (config.usePrimitives) {
            code.append(indent).append("trade.setPrice(order.getPrice()); // double\n")
                    .append(indent).append("trade.setVolume(order.getVolume()); // int\n");
        } else {
            code.append(indent).append("trade.setPrice(order.getPrice()); // boxed Double\n")
                    .append(indent).append("trade.setVolume(order.getVolume()); // boxed Integer\n");
        }

        if ("offheap".equals(config.memoryAccess)) {
            code.append(indent).append("// simulate off-heap access\n")
                    .append(indent).append("buffer.putDouble(0, trade.getPrice());\n");
        }

        code.append(indent).append("trade.execute();\n");

        if (config.useObjectPool) {
            code.append(indent).append("tradePool.release(trade);\n");
        }

        // Close wrapping
        if (config.enableBatching && config.threading.equals("threadpool")) {
            code.append("            }\n        });\n");
        } else if (config.enableBatching || config.threading.equals("threadpool")) {
            code.append("        }\n");
            if (config.threading.equals("threadpool")) {
                code.append("        });\n");
            }
        } else {
            code.append("    }\n");
        }

        code.append("}\n");

        return code.toString();
    }


    public List<String> generateExplanation(BenchmarkConfig config) {
        List<String> explanation = new ArrayList<>();

        // GC Strategy
        switch (config.gc) {
            case "Epsilon":
                explanation.add("- **GC Strategy: Epsilon (No GC)** — No garbage collection will occur. This reduces GC pause times but risks OOM if allocations are too frequent.");
                break;
            case "ZGC":
                explanation.add("- **GC Strategy: ZGC** — A low-pause concurrent collector suitable for large heaps. GC pauses are minimal but slightly impact throughput.");
                break;
            case "G1GC":
            default:
                explanation.add("- **GC Strategy: G1GC** — Balances throughput and pause times. Suitable for general-purpose workloads but may introduce minor latency jitter.");
                break;
        }

        // Object Pool
        if (config.useObjectPool) {
            explanation.add("- **Object Pooling Enabled** — Objects are reused to avoid frequent allocations and reduce GC pressure.");
        } else {
            explanation.add("- **Object Pooling Disabled** — New objects are created per request, increasing GC pressure.");
        }

        // Primitives
        if (config.usePrimitives) {
            explanation.add("- **Primitive Fields Used** — Uses primitives like `int`/`double` to avoid boxing overhead and reduce allocations.");
        } else {
            explanation.add("- **Boxed Fields Used** — Uses boxed types like `Integer`/`Double`, which can cause more allocations and GC pressure.");
        }

        // Disable Allocations
        if (config.disableAllocations) {
            explanation.add("- **Allocations Disabled (Simulated)** — All heap allocations are avoided where possible.");
        }

        // Pre-touch Memory
        if (config.preTouchMemory) {
            explanation.add("- **Memory Pre-touch Enabled** — JVM pre-touches memory pages on startup to reduce runtime latency spikes.");
        }

        // Memory Access
        switch (config.memoryAccess) {
            case "direct":
                explanation.add("- **Direct Memory Access** — Uses `ByteBuffer.allocateDirect()` to reduce heap GC overhead.");
                break;
            case "offheap":
                explanation.add("- **Off-Heap Access** — Bypasses the heap entirely, reducing GC impact and improving memory locality.");
                break;
            case "onheap":
            default:
                explanation.add("- **On-Heap Memory** — Uses standard heap memory with normal GC implications.");
                break;
        }

        // Threading
        if ("threadpool".equals(config.threading)) {
            explanation.add("- **Thread Pooling Enabled** — Uses a shared thread pool to improve throughput via parallelism.");
        } else {
            explanation.add("- **Single-threaded Execution** — Easier to reason about but may be slower under high throughput.");
        }

        // Thread Pinning
        if (config.pinThreads) {
            explanation.add("- **Threads Pinned to Cores** — Improves CPU cache locality and avoids context switching.");
        }

        // Batching
        if (config.enableBatching) {
            explanation.add("- **Batching Enabled** — Processes orders in groups to improve throughput by reducing per-item overhead.");
            explanation.add("  - **Batch Size:** " + config.batchSize);
        } else {
            explanation.add("- **Batching Disabled** — Each order is processed individually, which may reduce throughput.");
        }

        // Escape Analysis
        if (config.escapeAnalysisDisabled) {
            explanation.add("- **Escape Analysis Disabled (Simulated)** — Prevents JIT from optimizing object lifetimes, simulating real allocation costs.");
        }

        // Simulated Load
        if (config.simulateLoad) {
            explanation.add("- **System Load Simulated** — Introduces background CPU pressure to reflect realistic environments.");
        }

        return explanation;
    }

}
