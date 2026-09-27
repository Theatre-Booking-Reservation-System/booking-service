package com.theatre.bookingservice.repository;

import com.theatre.bookingservice.repository.model.Booking;
import com.theatre.bookingservice.util.BookingStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByBookingRef(String bookingRef);

    List<Booking> findByPatronIdOrderByCreatedAtDesc(UUID patronId);

    // Bookings for a performance in the given states (used to derive booked seats).
    List<Booking> findByPerformanceIdAndStatusIn(UUID performanceId, Collection<BookingStatus> statuses);

    // Most recent bookings across all patrons (for the admin dashboard "Recent Bookings" widget).
    List<Booking> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Bookings created on/after the given instant, whose status is in the given set.
    // Used to build the monthly "Booking Overview" chart.
    List<Booking> findByCreatedAtGreaterThanEqualAndStatusIn(
            OffsetDateTime createdAtFrom, Collection<BookingStatus> statuses);

    // Total number of bookings whose status is in the given set.
    @Query("select count(b) from Booking b where b.status in :statuses")
    long countByStatuses(@Param("statuses") Collection<BookingStatus> statuses);

    // Total revenue from bookings whose payment status marks them as paid.
    @Query("select coalesce(sum(b.totalLkr), 0) from Booking b "
            + "where b.paymentStatus = com.theatre.bookingservice.util.PaymentStatus.PAID")
    BigDecimal sumPaidRevenue();
}
