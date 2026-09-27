package com.theatre.bookingservice.repository.model;

import com.theatre.bookingservice.util.BookingStatus;
import com.theatre.bookingservice.util.PaymentMethod;
import com.theatre.bookingservice.util.PaymentStatus;
import com.theatre.bookingservice.util.TicketType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "booking")
@Getter
@Setter
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "booking_id", updatable = false, nullable = false)
    private UUID bookingId;

    @Column(name = "booking_ref", nullable = false, unique = true, length = 30)
    private String bookingRef;

    // Registered patron placing the booking (soft ref to identity_db). Always set:
    // bookings are for registered users only.
    @Column(name = "patron_id", nullable = false)
    private UUID patronId;

    // Soft ref to catalogue_db performance.
    @Column(name = "performance_id", nullable = false)
    private UUID performanceId;

    // The booked seats, serialised as JSON in a single column. Replaces booking_line.
    @Convert(converter = BookingSeatsConverter.class)
    @Column(name = "seats", nullable = false, columnDefinition = "text")
    private List<BookingSeat> seats = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type", nullable = false, length = 10)
    private TicketType ticketType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "is_flagged", nullable = false)
    private Boolean isFlagged;

    @Column(name = "total_lkr", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalLkr;

    // ----- Payment (simulated). Full PAN / CVV are NEVER persisted. -----
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    // Only the last 4 digits of the card are retained.
    @Column(name = "card_last4", length = 4)
    private String cardLast4;

    @Column(name = "card_holder_name", length = 120)
    private String cardHolderName;

    // Opaque gateway reference produced by the (simulated) authorisation.
    @Column(name = "payment_token", length = 200)
    private String paymentToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
