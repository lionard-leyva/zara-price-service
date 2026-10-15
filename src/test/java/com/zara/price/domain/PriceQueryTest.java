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

    @ParameterizedTest(name = "brandId={0}, productId={1} -> \"{2}\"")
    @CsvSource(delimiter = '|', value = {
            "0     | 35455 | brandId must be positive",
            "-1    | 35455 | brandId must be positive",
            "1     | 0     | productId must be positive",
            "1     | -1    | productId must be positive"
    })
    void rejectsIdsThatAreNotPositive(long brandId, long productId, String message) {
        assertThatThrownBy(() -> new PriceQuery(brandId, productId, APPLICATION_DATE))
                .isInstanceOf(InvalidPriceQueryException.class)
                .hasMessage(message);
    }

    @Test
    void rejectsAMissingBrandId() {
        assertThatThrownBy(() -> new PriceQuery(null, 35455L, APPLICATION_DATE))
                .isInstanceOf(InvalidPriceQueryException.class)
                .hasMessage("brandId is required");
    }

    @Test
    void rejectsAMissingProductId() {
        assertThatThrownBy(() -> new PriceQuery(1L, null, APPLICATION_DATE))
                .isInstanceOf(InvalidPriceQueryException.class)
                .hasMessage("productId is required");
    }

    @Test
    void rejectsAMissingApplicationDate() {
        assertThatThrownBy(() -> new PriceQuery(1L, 35455L, null))
                .isInstanceOf(InvalidPriceQueryException.class)
                .hasMessage("applicationDate is required");
    }
}
