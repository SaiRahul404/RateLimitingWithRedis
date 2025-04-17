package com.example.RateLimiting.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FallbackController {

    @RequestMapping("/rate-limit-exceeded")
    public ResponseEntity<Map<String, String>> rateLimitExceeded() {
        System.out.println("Fallback controller triggered");
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Too many requests");
        errorResponse.put("message", "You have exceeded the rate limit. Please try again later.");
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
    }
}

