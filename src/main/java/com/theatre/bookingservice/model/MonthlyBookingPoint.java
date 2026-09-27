package com.theatre.bookingservice.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MonthlyBookingPoint {

    // ISO month, e.g. "2026-06".
    private String month;

    // Short human-readable label, e.g. "Jun".
    private String label;

    private long bookings;

    private BigDecimal revenue;
}
