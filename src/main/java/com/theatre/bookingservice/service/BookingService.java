package com.theatre.bookingservice.service;

import com.theatre.bookingservice.exception.ServiceException;
import com.theatre.bookingservice.model.BookingItem;
import com.theatre.bookingservice.model.BookingLineItem;
import com.theatre.bookingservice.model.BookingLineRequest;
import com.theatre.bookingservice.model.BookingListResponse;
import com.theatre.bookingservice.model.BookingRequest;
import com.theatre.bookingservice.model.BookingResponse;
import com.theatre.bookingservice.repository.BookingRepository;
import com.theatre.bookingservice.repository.model.Booking;
import com.theatre.bookingservice.repository.model.BookingLine;
import com.theatre.bookingservice.util.BookingStatus;
import com.theatre.bookingservice.util.ErrorCode;
import com.theatre.bookingservice.util.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private static final DateTimeFormatter REF_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BookingRepository bookingRepository;
    private final CryptoService cryptoService;

    @Transactional
    public BookingResponse createBooking(BookingRequest request, String actor) {
        validateRequest(request);

        Booking booking = new Booking();
        booking.setBookingRef(generateBookingRef());
        booking.setPatronId(request.getPatronId());
        booking.setGuestEmail(request.getPatronId() == null ? request.getGuestEmail() : null);
        booking.setPerformanceId(request.getPerformanceId());
        booking.setStatus(BookingStatus.PENDING);
        booking.setIsFlagged(Boolean.FALSE);
        booking.setPaymentToken(request.getPaymentToken());
        booking.setPaymentStatus(PaymentStatus.UNPAID);
        booking.setCreatedAt(OffsetDateTime.now());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal vat = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (BookingLineRequest lineRequest : request.getLines()) {
            BookingLine line = toBookingLine(lineRequest);
            booking.addLine(line);

            subtotal = subtotal.add(nonNull(line.getBasePriceLkr()));
            discount = discount.add(nonNull(line.getConcessionDiscLkr()))
                    .add(nonNull(line.getLoyaltyDiscLkr()));
            vat = vat.add(nonNull(line.getVatLkr()));
            total = total.add(nonNull(line.getFinalPriceLkr()));
        }

        booking.setSubtotalLkr(subtotal);
        booking.setDiscountLkr(discount);
        booking.setVatLkr(vat);
        booking.setTotalLkr(total);

        return toBookingResponse(bookingRepository.save(booking));
    }

    public BookingResponse getBookingByRef(String bookingRef) {
        return bookingRepository.findByBookingRef(bookingRef)
                .map(this::toBookingResponse)
                .orElseThrow(() -> new ServiceException(ErrorCode.BOOKING_NOT_FOUND));
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

    @Transactional
    public BookingResponse cancelBooking(UUID bookingId, String actor) {
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

        return toBookingResponse(bookingRepository.save(booking));
    }

    private void validateRequest(BookingRequest request) {
        if (request == null
                || request.getPerformanceId() == null
                || request.getLines() == null
                || request.getLines().isEmpty()) {
            throw new ServiceException(ErrorCode.INVALID_BOOKING_REQUEST);
        }
        // A booking belongs to either a patron or an identified guest.
        if (request.getPatronId() == null
                && (request.getGuestEmail() == null || request.getGuestEmail().isBlank())) {
            throw new ServiceException(ErrorCode.INVALID_BOOKING_REQUEST);
        }
        for (BookingLineRequest line : request.getLines()) {
            if (line.getPerfSeatId() == null
                    || line.getSeatRef() == null || line.getSeatRef().isBlank()
                    || line.getZoneName() == null || line.getZoneName().isBlank()
                    || line.getSessionType() == null || line.getSessionType().isBlank()
                    || line.getBasePriceLkr() == null
                    || line.getFinalPriceLkr() == null) {
                throw new ServiceException(ErrorCode.INVALID_BOOKING_REQUEST);
            }
        }
    }

    private BookingLine toBookingLine(BookingLineRequest request) {
        BookingLine line = new BookingLine();
        line.setPerfSeatId(request.getPerfSeatId());
        line.setSeatRef(request.getSeatRef());
        line.setZoneName(request.getZoneName());
        line.setSessionType(request.getSessionType());
        line.setConcessionType(request.getConcessionType());
        // NIC/passport is only stored (encrypted) when a concession is claimed.
        if (request.getConcessionType() != null) {
            line.setNicPassportEnc(cryptoService.encrypt(request.getNicPassport()));
        }
        line.setBasePriceLkr(request.getBasePriceLkr());
        line.setConcessionDiscLkr(nonNull(request.getConcessionDiscLkr()));
        line.setLoyaltyDiscLkr(nonNull(request.getLoyaltyDiscLkr()));
        line.setVatLkr(nonNull(request.getVatLkr()));
        line.setFinalPriceLkr(request.getFinalPriceLkr());
        return line;
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

    private BigDecimal nonNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BookingResponse toBookingResponse(Booking booking) {
        List<BookingLineItem> lines = booking.getLines().stream()
                .map(this::toBookingLineItem)
                .toList();

        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .bookingRef(booking.getBookingRef())
                .patronId(booking.getPatronId())
                .guestEmail(booking.getGuestEmail())
                .performanceId(booking.getPerformanceId())
                .status(booking.getStatus())
                .isFlagged(booking.getIsFlagged())
                .subtotalLkr(booking.getSubtotalLkr())
                .discountLkr(booking.getDiscountLkr())
                .vatLkr(booking.getVatLkr())
                .totalLkr(booking.getTotalLkr())
                .paymentToken(booking.getPaymentToken())
                .paymentStatus(booking.getPaymentStatus())
                .createdAt(booking.getCreatedAt())
                .lines(lines)
                .build();
    }

    private BookingLineItem toBookingLineItem(BookingLine line) {
        return BookingLineItem.builder()
                .lineId(line.getLineId())
                .perfSeatId(line.getPerfSeatId())
                .seatRef(line.getSeatRef())
                .zoneName(line.getZoneName())
                .sessionType(line.getSessionType())
                .concessionType(line.getConcessionType())
                .basePriceLkr(line.getBasePriceLkr())
                .concessionDiscLkr(line.getConcessionDiscLkr())
                .loyaltyDiscLkr(line.getLoyaltyDiscLkr())
                .vatLkr(line.getVatLkr())
                .finalPriceLkr(line.getFinalPriceLkr())
                .build();
    }

    private BookingItem toBookingItem(Booking booking) {
        return BookingItem.builder()
                .bookingId(booking.getBookingId())
                .bookingRef(booking.getBookingRef())
                .performanceId(booking.getPerformanceId())
                .status(booking.getStatus())
                .paymentStatus(booking.getPaymentStatus())
                .totalLkr(booking.getTotalLkr())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
