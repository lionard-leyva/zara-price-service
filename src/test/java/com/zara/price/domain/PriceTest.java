package com.zara.price.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class PriceTest {

    private static final LocalDateTime JUN_14 = LocalDateTime.of(2020, 6, 14, 0, 0);
    private static final LocalDateTime JUN_15 = LocalDateTime.of(2020, 6, 15, 0, 0);

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

    private static Price price(long id, int priority, LocalDateTime startDate) {
        return new Price(
                id,
                1L,
                35455L,
                (int) id,
                startDate,
                LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                priority,
                BigDecimal.valueOf(10),
                "EUR");
    }
}
