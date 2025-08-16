package com.hftbench.latency_lab.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HealthController {
    @GetMapping("/api/health")
    public String health() {
        return "ok";
    }
}
