package com.hftbench.latency_lab.LowLatencyNoGc;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BenchmarkController {

    private final BenchmarkRunner runner = new BenchmarkRunner();

    @PostMapping("/run-benchmark")
    public BenchmarkResult runBenchmark(@RequestBody BenchmarkConfig config) {
        return runner.run(config); // Real benchmark execution
    }

    @PostMapping("/generate-code-snippet")
    public String generateCode(@RequestBody BenchmarkConfig config) {
        return CodeGenerator.generate(config);
    }

    @PostMapping("/generate-explanation")
    public List<String> generateExplanationText(@RequestBody BenchmarkConfig config) {
        return ExplanationGenerator.generate(config);
    }
}
