package com.theatre.bookingservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeatZoneClient {

    private final RestClient restClient;

    public SeatZoneClient(RestClient.Builder restClientBuilder,
                          @Value("${clients.seat.base-url:http://localhost:8083/seat-service}")
                          String seatBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(seatBaseUrl).build();
    }

    /**
     * Returns per-zone matinee/evening uplift percentages, keyed by zone name
     * (the booking payload carries {@code zoneName} per seat, so zone name is the
     * join key). Returns an empty map if the lookup fails.
     */
    public Map<String, ZonePricing> getZonePricingByName(String bearerToken) {
        Map<String, ZonePricing> pricing = new HashMap<>();
        try {
            SeatZoneListPayload payload = restClient.get()
                    .uri("/seat-zones")
                    .headers(headers -> applyAuth(headers, bearerToken))
                    .retrieve()
                    .body(SeatZoneListPayload.class);

            if (payload != null && payload.seatZones() != null) {
                for (SeatZonePayload zone : payload.seatZones()) {
                    if (zone.zoneName() != null) {
                        pricing.put(zone.zoneName(), new ZonePricing(zone.matineePct(), zone.eveningPct()));
                    }
                }
            }
        } catch (Exception e) {
            return pricing;
        }
        return pricing;
    }

    private void applyAuth(HttpHeaders headers, String bearerToken) {
        if (bearerToken != null && !bearerToken.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, bearerToken);
        }
    }

    public record ZonePricing(BigDecimal matineePct, BigDecimal eveningPct) {
    }

    // Minimal projections of the seat-service response (extra fields are ignored).
    private record SeatZoneListPayload(List<SeatZonePayload> seatZones) {
    }

    private record SeatZonePayload(String zoneName, BigDecimal matineePct, BigDecimal eveningPct) {
    }
}
