package com.theatre.bookingservice.repository.model;

import com.theatre.bookingservice.util.BookingStatus;
import com.theatre.bookingservice.util.PaymentStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

    // Soft ref to identity_db patron; NULL if guest booking.
    @Column(name = "patron_id")
    private UUID patronId;

    // Populated when patron_id is NULL.
    @Column(name = "guest_email", length = 320)
    private String guestEmail;

    // Soft ref to catalogue_db performance.
    @Column(name = "performance_id", nullable = false)
    private UUID performanceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "is_flagged", nullable = false)
    private Boolean isFlagged;

    @Column(name = "subtotal_lkr", precision = 10, scale = 2)
    private BigDecimal subtotalLkr;

    @Column(name = "discount_lkr", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountLkr;

    @Column(name = "vat_lkr", precision = 10, scale = 2)
    private BigDecimal vatLkr;

    @Column(name = "total_lkr", precision = 10, scale = 2)
    private BigDecimal totalLkr;

    // Gateway reference, never a card number.
    @Column(name = "payment_token", length = 200)
    private String paymentToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BookingLine> lines = new ArrayList<>();

    public void addLine(BookingLine line) {
        line.setBooking(this);
        this.lines.add(line);
    }
}
