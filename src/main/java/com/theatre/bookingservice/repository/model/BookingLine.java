package com.theatre.bookingservice.repository.model;

import com.theatre.bookingservice.util.ConcessionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "booking_line")
@Getter
@Setter
@NoArgsConstructor
public class BookingLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "line_id", updatable = false, nullable = false)
    private UUID lineId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    // Soft ref to seat_db performance_seat.
    @Column(name = "perf_seat_id", nullable = false)
    private UUID perfSeatId;

    // Denormalised label e.g. 'Stalls E12'.
    @Column(name = "seat_ref", nullable = false, length = 20)
    private String seatRef;

    // Denormalised at booking time.
    @Column(name = "zone_name", nullable = false, length = 100)
    private String zoneName;

    // Denormalised session label.
    @Column(name = "session_type", nullable = false, length = 10)
    private String sessionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "concession_type", length = 15)
    private ConcessionType concessionType;

    // AES-256 encrypted; NULL when no concession.
    @Column(name = "nic_passport_enc")
    private byte[] nicPassportEnc;

    @Column(name = "base_price_lkr", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePriceLkr;

    @Column(name = "concession_disc_lkr", nullable = false, precision = 10, scale = 2)
    private BigDecimal concessionDiscLkr;

    @Column(name = "loyalty_disc_lkr", nullable = false, precision = 10, scale = 2)
    private BigDecimal loyaltyDiscLkr;

    @Column(name = "vat_lkr", nullable = false, precision = 10, scale = 2)
    private BigDecimal vatLkr;

    @Column(name = "final_price_lkr", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalPriceLkr;
}
