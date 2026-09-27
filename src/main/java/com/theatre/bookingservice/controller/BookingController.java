package com.theatre.bookingservice.controller;

import com.theatre.bookingservice.config.AuthenticatedUser;
import com.theatre.bookingservice.model.BookingListResponse;
import com.theatre.bookingservice.model.BookingRequest;
import com.theatre.bookingservice.model.BookingResponse;
import com.theatre.bookingservice.model.BookingSummaryResponse;
import com.theatre.bookingservice.model.PerformanceBookedSeatsResponse;
import com.theatre.bookingservice.model.RecentBookingsResponse;
import com.theatre.bookingservice.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Create, look up and cancel theatre bookings")
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Create a booking",
            description = "Creates a CONFIRMED booking for the authenticated registered patron. Payment is "
                    + "simulated; only the card's last four digits are retained. Returns a QR-encoded "
                    + "confirmation with the production name, performance date/time, seats and total.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking created"),
            @ApiResponse(responseCode = "400", description = "Invalid booking request"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT / not a registered user"),
            @ApiResponse(responseCode = "402", description = "Payment authorisation failed"),
            @ApiResponse(responseCode = "409", description = "One or more selected seats are already booked")
    })
    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> createBooking(
            @RequestBody BookingRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(hidden = true) @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
            String authorization) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(request, user, authorization));
    }

    @Operation(summary = "Get a booking by reference",
            description = "Returns a single booking identified by its human-readable booking reference "
                    + "(e.g. STB-20260913-00847), including its seats.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "No booking exists for the given reference")
    })
    @GetMapping("/bookings/{ref}")
    public ResponseEntity<BookingResponse> getBookingByRef(
            @Parameter(description = "Human-readable booking reference") @PathVariable String ref) {
        return ResponseEntity.ok(bookingService.getBookingByRef(ref));
    }

    @Operation(summary = "List bookings for a patron",
            description = "Returns all bookings placed by the given patron, most recent first.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bookings returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping("/patrons/{id}/bookings")
    public ResponseEntity<BookingListResponse> getBookingsByPatronId(
            @Parameter(description = "Unique identifier of the patron") @PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.getBookingsByPatronId(id));
    }

    @Operation(summary = "List booked seats for a performance",
            description = "Returns the seats already booked for a performance (from active bookings). "
                    + "Consumed by seat-service to derive per-seat availability.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booked seats returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping("/performances/{id}/bookings")
    public ResponseEntity<PerformanceBookedSeatsResponse> getBookingsByPerformanceId(
            @Parameter(description = "Unique identifier of the performance") @PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.getBookingsByPerformanceId(id));
    }

    @Operation(summary = "Recent bookings (admin dashboard)",
            description = "Returns the most recent bookings across all patrons, most recent first, for "
                    + "the admin dashboard 'Recent Bookings' widget. Each row is enriched with the "
                    + "customer name and the show name / performance date-time. Defaults to 5 rows; "
                    + "override with the 'limit' query parameter.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recent bookings returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping("/bookings/recent")
    public ResponseEntity<RecentBookingsResponse> getRecentBookings(
            @Parameter(description = "Maximum number of bookings to return (default 5)")
            @RequestParam(defaultValue = "5") int limit,
            @Parameter(hidden = true) @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
            String authorization) {
        return ResponseEntity.ok(bookingService.getRecentBookings(limit, authorization));
    }

    @Operation(summary = "Booking summary (admin dashboard)",
            description = "Returns aggregate figures for the admin dashboard: total (active) bookings, "
                    + "total paid revenue and a per-month bookings/revenue breakdown for the "
                    + "'Booking Overview' chart (current month plus the prior five).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping("/bookings/summary")
    public ResponseEntity<BookingSummaryResponse> getSummary() {
        return ResponseEntity.ok(bookingService.getSummary());
    }

    @Operation(summary = "Cancel a booking",
            description = "Cancels an existing booking identified by its UUID. Paid bookings are marked "
                    + "for refund. Already-cancelled or expired bookings cannot be cancelled again.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking cancelled"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "No booking exists for the given id"),
            @ApiResponse(responseCode = "409", description = "Booking cannot be cancelled in its current state")
    })
    @PutMapping("/bookings/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @Parameter(description = "Unique identifier of the booking") @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, user));
    }
}
