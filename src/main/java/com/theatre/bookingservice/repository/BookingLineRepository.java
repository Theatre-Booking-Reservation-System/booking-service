package com.theatre.bookingservice.repository;

import com.theatre.bookingservice.repository.model.BookingLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookingLineRepository extends JpaRepository<BookingLine, UUID> {
}
