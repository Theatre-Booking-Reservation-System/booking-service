package com.theatre.bookingservice.service;

import com.theatre.bookingservice.exception.ServiceException;
import com.theatre.bookingservice.util.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Service
public class CatalogueClient {

    private final RestClient restClient;

    public CatalogueClient(RestClient.Builder restClientBuilder,
                           @Value("${clients.catalogue.base-url:http://localhost:8082/catalogue-service}")
                           String catalogueBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(catalogueBaseUrl).build();
    }

    public PerformanceDetails getPerformance(UUID performanceId, String bearerToken) {
        try {
            PerformancePayload performance = restClient.get()
                    .uri("/performances/{id}", performanceId)
                    .headers(headers -> applyAuth(headers, bearerToken))
                    .retrieve()
                    .body(PerformancePayload.class);

            if (performance == null || performance.productionId() == null) {
                throw new ServiceException(ErrorCode.PERFORMANCE_LOOKUP_FAILED);
            }

            ProductionPayload production = restClient.get()
                    .uri("/productions/{id}", performance.productionId())
                    .headers(headers -> applyAuth(headers, bearerToken))
                    .retrieve()
                    .body(ProductionPayload.class);

            String title = production != null ? production.title() : null;
            BigDecimal baseTicketCost = production != null ? production.baseTicketCost() : null;
            return new PerformanceDetails(title, baseTicketCost, performance.date(), performance.time());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.PERFORMANCE_LOOKUP_FAILED);
        }
    }

    private void applyAuth(HttpHeaders headers, String bearerToken) {
        if (bearerToken != null && !bearerToken.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
        }
    }

    public record PerformanceDetails(String productionName, BigDecimal baseTicketCost,
                                     LocalDate date, LocalTime time) {
    }

    // Minimal projections of the catalogue responses (extra fields are ignored).
    private record PerformancePayload(UUID performanceId, UUID productionId, LocalDate date, LocalTime time) {
    }

    private record ProductionPayload(UUID productionId, String title, BigDecimal baseTicketCost) {
    }
}
