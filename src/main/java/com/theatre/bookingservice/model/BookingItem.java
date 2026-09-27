package com.theatre.bookingservice.model;

import com.theatre.bookingservice.util.BookingStatus;
import com.theatre.bookingservice.util.PaymentStatus;
import com.theatre.bookingservice.util.TicketType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class BookingItem {
    private UUID bookingId;
    private String bookingRef;
    private UUID performanceId;
    private List<BookingSeatItem> seats;
    private TicketType ticketType;
    private BookingStatus status;
    private PaymentStatus paymentStatus;
    private BigDecimal totalLkr;
    private OffsetDateTime createdAt;
}
