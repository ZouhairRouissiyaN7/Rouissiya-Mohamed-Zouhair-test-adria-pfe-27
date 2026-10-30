
package com.adria.transaction_service.web;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/internal/circuit-breakers")
@RequiredArgsConstructor
@Profile("dev")
public class CircuitBreakerStatusController {

    private final CircuitBreakerRegistry registry;

    @GetMapping
    public Map<String, Object> getCircuitStatus() {

        Map<String, Object> status = new TreeMap<>();

        registry.getAllCircuitBreakers().forEach(cb -> {
            var metrics = cb.getMetrics();

            status.put(cb.getName(), Map.of(
                    "state", cb.getState().name(),
                    "failureRate", metrics.getFailureRate(),
                    "bufferedCalls", metrics.getNumberOfBufferedCalls(),
                    "failedCalls", metrics.getNumberOfFailedCalls(),
                    "successfulCalls", metrics.getNumberOfSuccessfulCalls(),
                    "notPermittedCalls", metrics.getNumberOfNotPermittedCalls()));
        });

        return status;
    }
}
