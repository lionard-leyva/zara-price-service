package com.zara.price.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PriceQueryTest {

    private static final LocalDateTime APPLICATION_DATE = LocalDateTime.of(2020, 6, 14, 10, 0);

    @Test
    void keepsItsValues() {
        PriceQuery query = new PriceQuery(1L, 35455L, APPLICATION_DATE);

        assertThat(query.brandId()).isEqualTo(1L);
        assertThat(query.productId()).isEqualTo(35455L);
        assertThat(query.applicationDate()).isEqualTo(APPLICATION_DATE);
    }

    @ParameterizedTest(name = "brandId={0}, productId={1} is rejected")
    @CsvSource({
            "0, 35455",
            "-1, 35455",
            "1, 0",
            "1, -1"
    })
    void rejectsIdsThatAreNotPositive(long brandId, long productId) {
        assertThatThrownBy(() -> new PriceQuery(brandId, productId, APPLICATION_DATE))
                .isInstanceOf(InvalidPriceQueryException.class);
    }

    @Test
    void rejectsAMissingApplicationDate() {
        assertThatThrownBy(() -> new PriceQuery(1L, 35455L, null))
                .isInstanceOf(InvalidPriceQueryException.class);
    }
}
