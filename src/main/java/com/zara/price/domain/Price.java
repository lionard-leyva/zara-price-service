package com.zara.price.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record Price(
        long id,
        long brandId,
        long productId,
        int priceList,
        LocalDateTime startDate,
        LocalDateTime endDate,
        int priority,
        BigDecimal price,
        String currency) {

    public Price {
        Objects.requireNonNull(startDate, "startDate is required");
        Objects.requireNonNull(endDate, "endDate is required");
        Objects.requireNonNull(price, "price is required");
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }
        if (price.signum() < 0) {
            throw new IllegalArgumentException("price must not be negative");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency is required");
        }
    }

    public static Optional<Price> mostPrioritized(List<Price> candidates) {
        return candidates.stream()
                .reduce((best, next) -> next.takesPrecedenceOver(best) ? next : best);
    }

    private boolean takesPrecedenceOver(Price other) {
        if (priority != other.priority) {
            return priority > other.priority;
        }
        if (!startDate.equals(other.startDate)) {
            return startDate.isAfter(other.startDate);
        }
        return id > other.id;
    }
}
