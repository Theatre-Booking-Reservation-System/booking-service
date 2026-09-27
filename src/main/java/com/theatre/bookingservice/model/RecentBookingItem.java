package com.theatre.bookingservice.model;

import com.theatre.bookingservice.util.BookingStatus;
import com.theatre.bookingservice.util.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class RecentBookingItem {
    private UUID bookingId;
    private String bookingRef;
    private UUID patronId;
    private String customerName;
    private UUID performanceId;
    private String showName;
    private LocalDate performanceDate;
    private LocalTime performanceTime;
    private BigDecimal totalLkr;
    private BookingStatus status;
    private PaymentStatus paymentStatus;
    private OffsetDateTime createdAt;
}
