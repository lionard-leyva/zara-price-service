package com.zara.price.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PriceTest {

    private static final LocalDateTime JUN_14 = LocalDateTime.of(2020, 6, 14, 0, 0);
    private static final LocalDateTime JUN_15 = LocalDateTime.of(2020, 6, 15, 0, 0);
    private static final LocalDateTime END_OF_YEAR = LocalDateTime.of(2020, 12, 31, 23, 59, 59);

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
        BigDecimal negativeAmount = BigDecimal.valueOf(-0.01);

        assertThatThrownBy(() -> price(JUN_14, END_OF_YEAR, negativeAmount, "EUR"))
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

    private static Price price(LocalDateTime startDate, LocalDateTime endDate, BigDecimal amount, String currency) {
        return new Price(1L, 1L, 35455L, 1, startDate, endDate, 0, amount, currency);
    }
}
