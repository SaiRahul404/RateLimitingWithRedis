package com.example.RateLimiting.helper;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class ErrorResponseBuilder {

    public static Mono<Void> writeJsonError(ServerWebExchange exchange,
                                            int statusCode,
                                            String errorCode,
                                            String message) {
        exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(statusCode));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("""
            {
              "status": "error",
              "code": "%s",
              "message": "%s",
              "timestamp": "%s"
            }
            """, errorCode, message, Instant.now().toString());

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}

