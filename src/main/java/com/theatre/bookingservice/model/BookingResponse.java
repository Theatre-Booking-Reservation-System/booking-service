package com.theatre.bookingservice.model;

import com.theatre.bookingservice.util.BookingStatus;
import com.theatre.bookingservice.util.PaymentStatus;
import com.theatre.bookingservice.util.TicketType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class BookingResponse extends CommonResponse {
    private UUID bookingId;
    private String bookingRef;
    private UUID patronId;
    private UUID performanceId;

    // Enriched from catalogue-service for the confirmation / QR.
    private String productionName;
    private LocalDate performanceDate;
    private LocalTime performanceTime;

    private List<BookingSeatItem> seats;
    private TicketType ticketType;
    private BigDecimal totalLkr;

    private BookingStatus status;
    private PaymentStatus paymentStatus;
    private String cardLast4;
    private OffsetDateTime createdAt;

    // QR code (PNG) as a base64 data URI encoding the booking summary.
    private String qrCode;
}
