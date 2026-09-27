package com.theatre.bookingservice.repository;

import com.theatre.bookingservice.repository.model.Booking;
import com.theatre.bookingservice.util.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByBookingRef(String bookingRef);

    List<Booking> findByPatronIdOrderByCreatedAtDesc(UUID patronId);

    // Bookings for a performance in the given states (used to derive booked seats).
    List<Booking> findByPerformanceIdAndStatusIn(UUID performanceId, Collection<BookingStatus> statuses);
}
