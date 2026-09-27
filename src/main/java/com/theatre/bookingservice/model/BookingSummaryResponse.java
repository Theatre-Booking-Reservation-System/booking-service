package com.theatre.bookingservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class BookingSummaryResponse extends CommonResponse {

    // "Total Bookings" stat card (active bookings: PENDING or CONFIRMED).
    private long totalBookings;

    // "Total Revenue" stat card (sum of paid bookings, in LKR).
    private BigDecimal totalRevenue;

    // "Booking Overview" chart, oldest month first.
    private List<MonthlyBookingPoint> bookingOverview;
}
