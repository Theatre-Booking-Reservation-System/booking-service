package com.theatre.bookingservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class IdentityClient {

    private final RestClient restClient;

    public IdentityClient(RestClient.Builder restClientBuilder,
                          @Value("${clients.identity.base-url:http://localhost:8081/identity-service}")
                          String identityBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(identityBaseUrl).build();
    }

    public Map<UUID, String> getPatronNames(List<UUID> patronIds, String bearerToken) {
        Map<UUID, String> names = new HashMap<>();
        if (patronIds == null || patronIds.isEmpty()) {
            return names;
        }
        try {
            PatronListPayload payload = restClient.get()
                    .uri("/patron/list")
                    .headers(headers -> applyAuth(headers, bearerToken))
                    .retrieve()
                    .body(PatronListPayload.class);

            if (payload != null && payload.patrons() != null) {
                for (PatronPayload patron : payload.patrons()) {
                    if (patron.patronId() != null) {
                        names.put(patron.patronId(), patron.name());
                    }
                }
            }
        } catch (Exception e) {
            // Enrichment is best-effort: fall back to no names rather than failing.
            return names;
        }
        return names;
    }

    private void applyAuth(HttpHeaders headers, String bearerToken) {
        if (bearerToken != null && !bearerToken.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
        }
    }

    // Minimal projections of the identity responses (extra fields are ignored).
    private record PatronListPayload(List<PatronPayload> patrons) {
    }

    private record PatronPayload(UUID patronId, String name) {
    }
}
