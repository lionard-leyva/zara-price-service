package com.zara.price.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PriceTest {

    private static final LocalDateTime JUN_14 = LocalDateTime.of(2020, 6, 14, 0, 0);
    private static final LocalDateTime JUN_15 = LocalDateTime.of(2020, 6, 15, 0, 0);
    private static final LocalDateTime END_OF_YEAR = LocalDateTime.of(2020, 12, 31, 23, 59, 59);

    @Test
    void mostPrioritizedReturnsTheHighestPriority() {
        Price base = price(1, 0, JUN_14);
        Price promotion = price(2, 1, JUN_14);

        assertThat(Price.mostPrioritized(List.of(base, promotion))).contains(promotion);
    }

    @Test
    void mostPrioritizedBreaksPriorityTiesWithTheLatestStartDate() {
        Price earlier = price(1, 1, JUN_14);
        Price later = price(2, 1, JUN_15);

        assertThat(Price.mostPrioritized(List.of(earlier, later))).contains(later);
    }

    @Test
    void mostPrioritizedBreaksRemainingTiesWithTheHighestId() {
        Price firstInserted = price(1, 1, JUN_14);
        Price lastInserted = price(2, 1, JUN_14);

        assertThat(Price.mostPrioritized(List.of(firstInserted, lastInserted))).contains(lastInserted);
    }

    @Test
    void mostPrioritizedIsEmptyWhenThereAreNoPrices() {
        assertThat(Price.mostPrioritized(List.of())).isEmpty();
    }

    @Test
    void rejectsAStartDateAfterTheEndDate() {
        assertThatThrownBy(() -> price(JUN_15, JUN_14, BigDecimal.TEN, "EUR"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsAnIntervalThatStartsAndEndsAtTheSameInstant() {
        assertThat(price(JUN_14, JUN_14, BigDecimal.TEN, "EUR")).isNotNull();
    }

    @Test
    void rejectsANegativePrice() {
        assertThatThrownBy(() -> price(JUN_14, END_OF_YEAR, BigDecimal.valueOf(-0.01), "EUR"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsAFreePrice() {
        assertThat(price(JUN_14, END_OF_YEAR, BigDecimal.ZERO, "EUR")).isNotNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsAMissingCurrency(String currency) {
        assertThatThrownBy(() -> price(JUN_14, END_OF_YEAR, BigDecimal.TEN, currency))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Price price(long id, int priority, LocalDateTime startDate) {
        return new Price(id, 1L, 35455L, (int) id, startDate, END_OF_YEAR, priority, BigDecimal.TEN, "EUR");
    }

    private static Price price(LocalDateTime startDate, LocalDateTime endDate, BigDecimal amount, String currency) {
        return new Price(1L, 1L, 35455L, 1, startDate, endDate, 0, amount, currency);
    }
}
