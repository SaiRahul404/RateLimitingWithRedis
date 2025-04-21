package com.example.RateLimiting.filters;

import com.example.RateLimiting.helper.ErrorResponseBuilder;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.JsonFactory;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class GoogleAuthFilter extends AbstractGatewayFilterFactory<GoogleAuthFilter.Config> {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String GOOGLE_ISSUER = "https://accounts.google.com";

    public GoogleAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            // Skip token check for public endpoints
            if (path.equals("/api/sample/token")) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ErrorResponseBuilder.writeJsonError(exchange,
                        HttpStatus.UNAUTHORIZED.value(),
                        "missing_auth_header",
                        "Authorization header is missing or invalid");
            }

            String token = authHeader.substring(7);

            try {
                GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        JSON_FACTORY)
                        .setIssuer(GOOGLE_ISSUER)
                        .build();

                GoogleIdToken idToken = verifier.verify(token);

                if (idToken == null) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                // Token is valid — proceed
                return chain.filter(exchange);

            } catch (Exception e) {
                exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                return exchange.getResponse().setComplete();
            }
        };
    }

    public static class Config {
        // Empty config
    }

}
