package com.theatre.bookingservice.service;

import com.theatre.bookingservice.config.AuthenticatedUser;
import com.theatre.bookingservice.exception.ServiceException;
import com.theatre.bookingservice.model.BookingItem;
import com.theatre.bookingservice.model.BookingListResponse;
import com.theatre.bookingservice.model.BookingRequest;
import com.theatre.bookingservice.model.BookingResponse;
import com.theatre.bookingservice.model.BookingSeatItem;
import com.theatre.bookingservice.model.PaymentDetails;
import com.theatre.bookingservice.model.PerformanceBookedSeatsResponse;
import com.theatre.bookingservice.model.SeatSelection;
import com.theatre.bookingservice.repository.BookingRepository;
import com.theatre.bookingservice.repository.model.Booking;
import com.theatre.bookingservice.repository.model.BookingSeat;
import com.theatre.bookingservice.service.CatalogueClient.PerformanceDetails;
import com.theatre.bookingservice.service.PaymentService.PaymentResult;
import com.theatre.bookingservice.util.BookingStatus;
import com.theatre.bookingservice.util.ErrorCode;
import com.theatre.bookingservice.util.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private static final DateTimeFormatter REF_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    // Bookings in these states occupy their seats.
    private static final List<BookingStatus> ACTIVE_STATES =
            List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final PaymentService paymentService;
    private final CatalogueClient catalogueClient;
    private final QrCodeService qrCodeService;

    /**
     * Creates a confirmed booking for a registered patron. Enforces authentication,
     * rejects seats already booked for the performance, simulates payment, and
     * returns a QR-encoded confirmation.
     */
    @Transactional
    public BookingResponse createBooking(BookingRequest request, AuthenticatedUser user, String bearerToken) {
        requireRegisteredPatron(user);
        validateRequest(request);
        UUID patronId = request.getPatronId();

        List<SeatSelection> requested = request.getSeats();
        assertSeatsAvailable(request.getPerformanceId(), requested);

        // Enrich from catalogue (also validates the performance exists) before charging.
        PerformanceDetails performance = catalogueClient.getPerformance(request.getPerformanceId(), bearerToken);

        BigDecimal total = calculateTotal(requested);
        PaymentResult payment = paymentService.authorise(request.getPaymentDetails(), total);

        Booking booking = new Booking();
        booking.setBookingRef(generateBookingRef());
        booking.setPatronId(patronId);
        booking.setPerformanceId(request.getPerformanceId());
        booking.setSeats(toBookingSeats(requested));
        booking.setTicketType(request.getTicketType());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setIsFlagged(Boolean.FALSE);
        booking.setTotalLkr(total);
        booking.setPaymentMethod(request.getPaymentMethod());
        booking.setCardLast4(payment.cardLast4());
        booking.setCardHolderName(cardHolder(request.getPaymentDetails()));
        booking.setPaymentToken(payment.paymentToken());
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setCreatedAt(OffsetDateTime.now());

        Booking saved = bookingRepository.save(booking);
        return toBookingResponse(saved, performance);
    }

    public BookingResponse getBookingByRef(String bookingRef) {
        Booking booking = bookingRepository.findByBookingRef(bookingRef)
                .orElseThrow(() -> new ServiceException(ErrorCode.BOOKING_NOT_FOUND));
        return toBookingResponse(booking, null);
    }

    public BookingListResponse getBookingsByPatronId(UUID patronId) {
        List<BookingItem> bookings = bookingRepository.findByPatronIdOrderByCreatedAtDesc(patronId)
                .stream()
                .map(this::toBookingItem)
                .toList();

        return BookingListResponse.builder()
                .bookings(bookings)
                .build();
    }

    public PerformanceBookedSeatsResponse getBookingsByPerformanceId(UUID performanceId) {
        List<Booking> bookings =
                bookingRepository.findByPerformanceIdAndStatusIn(performanceId, ACTIVE_STATES);

        List<UUID> seatIds = new ArrayList<>();
        List<String> seatRefs = new ArrayList<>();
        for (Booking booking : bookings) {
            for (BookingSeat seat : booking.getSeats()) {
                seatIds.add(seat.getSeatId());
                seatRefs.add(seat.getSeatRef());
            }
        }

        return PerformanceBookedSeatsResponse.builder()
                .performanceId(performanceId)
                .bookedSeatIds(seatIds)
                .bookedSeatRefs(seatRefs)
                .build();
    }

    @Transactional
    public BookingResponse cancelBooking(UUID bookingId, AuthenticatedUser user) {
        requireRegisteredPatron(user);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ServiceException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() == BookingStatus.CANCELLED_PATRON
                || booking.getStatus() == BookingStatus.CANCELLED_ADMIN) {
            throw new ServiceException(ErrorCode.BOOKING_ALREADY_CANCELLED);
        }

        if (booking.getStatus() == BookingStatus.EXPIRED) {
            throw new ServiceException(ErrorCode.BOOKING_NOT_CANCELLABLE);
        }

        booking.setStatus(BookingStatus.CANCELLED_PATRON);
        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            booking.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        return toBookingResponse(bookingRepository.save(booking), null);
    }

    private void requireRegisteredPatron(AuthenticatedUser user) {
        // Bookings are for authenticated, registered users only. The JWT filter only
        // establishes a principal for a valid token, so a present principal means a
        // registered user; anonymous/guest callers are rejected.
        if (user == null || user.subject() == null || user.subject().isBlank()) {
            throw new ServiceException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
    }

    private void validateRequest(BookingRequest request) {
        if (request == null
                || request.getPatronId() == null
                || request.getPerformanceId() == null
                || request.getSeats() == null
                || request.getSeats().isEmpty()
                || request.getTicketType() == null
                || request.getPaymentMethod() == null) {
            throw new ServiceException(ErrorCode.INVALID_BOOKING_REQUEST);
        }
        for (SeatSelection seat : request.getSeats()) {
            if (seat.getSeatId() == null
                    || seat.getSeatRef() == null || seat.getSeatRef().isBlank()
                    || seat.getZoneName() == null || seat.getZoneName().isBlank()
                    || seat.getSection() == null || seat.getSection().isBlank()) {
                throw new ServiceException(ErrorCode.INVALID_BOOKING_REQUEST);
            }
        }
    }

    private void assertSeatsAvailable(UUID performanceId, List<SeatSelection> requested) {
        Set<UUID> alreadyBooked = new HashSet<>(
                getBookingsByPerformanceId(performanceId).getBookedSeatIds());
        for (SeatSelection seat : requested) {
            if (alreadyBooked.contains(seat.getSeatId())) {
                throw new ServiceException(ErrorCode.SEAT_ALREADY_BOOKED);
            }
        }
    }

    private BigDecimal calculateTotal(List<SeatSelection> seats) {
        // Pricing is out of scope for this refactor; a flat per-seat placeholder is
        // used so the simulated payment has a positive amount to authorise.
        BigDecimal perSeat = new BigDecimal("1500.00");
        return perSeat.multiply(BigDecimal.valueOf(seats.size()));
    }

    private List<BookingSeat> toBookingSeats(List<SeatSelection> seats) {
        List<BookingSeat> result = new ArrayList<>(seats.size());
        for (SeatSelection seat : seats) {
            result.add(new BookingSeat(
                    seat.getSeatId(),
                    seat.getSeatRef(),
                    seat.getZoneName(),
                    seat.getSection()));
        }
        return result;
    }

    private String cardHolder(PaymentDetails details) {
        return details != null ? details.getCardHolderName() : null;
    }

    private String generateBookingRef() {
        String candidate;
        do {
            int sequence = ThreadLocalRandom.current().nextInt(0, 100000);
            candidate = "STB-" + OffsetDateTime.now(ZoneOffset.UTC).format(REF_DATE)
                    + "-" + String.format("%05d", sequence);
        } while (bookingRepository.findByBookingRef(candidate).isPresent());
        return candidate;
    }

    private List<BookingSeatItem> toSeatItems(Booking booking) {
        return booking.getSeats().stream()
                .map(seat -> BookingSeatItem.builder()
                        .seatId(seat.getSeatId())
                        .seatRef(seat.getSeatRef())
                        .zoneName(seat.getZoneName())
                        .section(seat.getSection())
                        .build())
                .toList();
    }

    private BookingResponse toBookingResponse(Booking booking, PerformanceDetails performance) {
        List<BookingSeatItem> seats = toSeatItems(booking);

        String productionName = performance != null ? performance.productionName() : null;
        LocalDate date = performance != null ? performance.date() : null;
        LocalTime time = performance != null ? performance.time() : null;

        String qr = qrCodeService.toPngDataUri(buildQrContent(booking, productionName, date, time));

        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .bookingRef(booking.getBookingRef())
                .patronId(booking.getPatronId())
                .performanceId(booking.getPerformanceId())
                .productionName(productionName)
                .performanceDate(date)
                .performanceTime(time)
                .seats(seats)
                .ticketType(booking.getTicketType())
                .totalLkr(booking.getTotalLkr())
                .status(booking.getStatus())
                .paymentStatus(booking.getPaymentStatus())
                .cardLast4(booking.getCardLast4())
                .createdAt(booking.getCreatedAt())
                .qrCode(qr)
                .build();
    }

    private String buildQrContent(Booking booking, String productionName, LocalDate date, LocalTime time) {
        String seatRefs = booking.getSeats().stream()
                .map(BookingSeat::getSeatRef)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        return "Booking: " + booking.getBookingRef()
                + "\nProduction: " + (productionName != null ? productionName : "-")
                + "\nWhen: " + (date != null ? date : "-") + " " + (time != null ? time : "")
                + "\nSeats: " + seatRefs
                + "\nTotal (LKR): " + booking.getTotalLkr();
    }

    private BookingItem toBookingItem(Booking booking) {
        return BookingItem.builder()
                .bookingId(booking.getBookingId())
                .bookingRef(booking.getBookingRef())
                .performanceId(booking.getPerformanceId())
                .seats(toSeatItems(booking))
                .ticketType(booking.getTicketType())
                .status(booking.getStatus())
                .paymentStatus(booking.getPaymentStatus())
                .totalLkr(booking.getTotalLkr())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
