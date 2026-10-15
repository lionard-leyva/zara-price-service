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
    void keepsItsValues() {
        Price price = price(JUN_14, END_OF_YEAR, new BigDecimal("35.50"), "EUR");

        assertThat(price.startDate()).isEqualTo(JUN_14);
        assertThat(price.endDate()).isEqualTo(END_OF_YEAR);
        assertThat(price.price()).isEqualByComparingTo("35.50");
        assertThat(price.currency()).isEqualTo("EUR");
    }

    @Test
    void rejectsAStartDateAfterTheEndDate() {
        assertThatThrownBy(() -> price(JUN_15, JUN_14, BigDecimal.TEN, "EUR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("startDate must not be after endDate");
    }

    @Test
    void acceptsAnIntervalThatStartsAndEndsAtTheSameInstant() {
        Price price = price(JUN_14, JUN_14, BigDecimal.TEN, "EUR");

        assertThat(price.startDate()).isEqualTo(price.endDate());
    }

    @Test
    void rejectsANegativePrice() {
        BigDecimal negativeAmount = new BigDecimal("-0.01");

        assertThatThrownBy(() -> price(JUN_14, END_OF_YEAR, negativeAmount, "EUR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("price must not be negative");
    }

    @Test
    void acceptsAFreePrice() {
        Price price = price(JUN_14, END_OF_YEAR, BigDecimal.ZERO, "EUR");

        assertThat(price.price()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsAMissingCurrency(String currency) {
        assertThatThrownBy(() -> price(JUN_14, END_OF_YEAR, BigDecimal.TEN, currency))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("currency is required");
    }

    @Test
    void rejectsAMissingStartDate() {
        assertThatThrownBy(() -> price(null, END_OF_YEAR, BigDecimal.TEN, "EUR"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("startDate is required");
    }

    @Test
    void rejectsAMissingEndDate() {
        assertThatThrownBy(() -> price(JUN_14, null, BigDecimal.TEN, "EUR"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("endDate is required");
    }

    @Test
    void rejectsAMissingPrice() {
        assertThatThrownBy(() -> price(JUN_14, END_OF_YEAR, null, "EUR"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("price is required");
    }

    private static Price price(LocalDateTime startDate, LocalDateTime endDate, BigDecimal amount, String currency) {
        return new Price(1L, 1L, 35455L, 1, startDate, endDate, 0, amount, currency);
    }
}
