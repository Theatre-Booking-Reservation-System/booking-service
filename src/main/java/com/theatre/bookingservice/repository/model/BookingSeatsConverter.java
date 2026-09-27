package com.theatre.bookingservice.repository.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class BookingSeatsConverter implements AttributeConverter<List<BookingSeat>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<BookingSeat>> TYPE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(List<BookingSeat> seats) {
        if (seats == null || seats.isEmpty()) {
            return "[]";
        }
        try {
            return MAPPER.writeValueAsString(seats);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialise booking seats", e);
        }
    }

    @Override
    public List<BookingSeat> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return MAPPER.readValue(json, TYPE);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialise booking seats", e);
        }
    }
}
