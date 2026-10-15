package com.zara.price.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.zara.price.domain.Price;
import com.zara.price.domain.PriceQuery;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class JpaPriceRepositoryTest {

    private static final long BRAND = 7L;
    private static final long PRODUCT = 42L;
    private static final LocalDateTime APPLICATION_DATE = LocalDateTime.of(2020, 6, 14, 10, 0);

    @Autowired
    private JpaPriceRepository repository;

    @Test
    @Sql(statements = {
            // lower priority but starts later: must lose to the higher priority
            "INSERT INTO PRICES (brand_id, product_id, price_list, start_date, end_date, priority, price, curr) "
                    + "VALUES (7, 42, 1, '2020-06-14 00:00:00', '2020-12-31 23:59:59', 0, 10.00, 'EUR')",
            "INSERT INTO PRICES (brand_id, product_id, price_list, start_date, end_date, priority, price, curr) "
                    + "VALUES (7, 42, 2, '2020-06-13 00:00:00', '2020-12-31 23:59:59', 1, 20.00, 'EUR')"
    })
    void prefersTheHighestPriorityOverTheLatestStart() {
        assertThat(repository.findApplicablePrice(query()).map(Price::priceList)).hasValue(2);
    }

    @Test
    @Sql(statements = {
            // same priority and start date: the most recently inserted (highest id) wins
            "INSERT INTO PRICES (brand_id, product_id, price_list, start_date, end_date, priority, price, curr) "
                    + "VALUES (7, 42, 1, '2020-06-14 00:00:00', '2020-12-31 23:59:59', 1, 10.00, 'EUR')",
            "INSERT INTO PRICES (brand_id, product_id, price_list, start_date, end_date, priority, price, curr) "
                    + "VALUES (7, 42, 2, '2020-06-14 00:00:00', '2020-12-31 23:59:59', 1, 20.00, 'EUR')"
    })
    void breaksTiesWithTheHighestId() {
        Price price = repository.findApplicablePrice(query()).orElseThrow();

        assertThat(price.priceList()).isEqualTo(2);
    }

    @Test
    @Sql(statements = {
            "INSERT INTO PRICES (brand_id, product_id, price_list, start_date, end_date, priority, price, curr) "
                    + "VALUES (7, 42, 1, '2020-06-14 00:00:00', '2020-12-31 23:59:59', 0, 10.55, 'EUR')"
    })
    void mapsAllFieldsOfTheMatchingPrice() {
        Price price = repository.findApplicablePrice(query()).orElseThrow();

        assertThat(price.brandId()).isEqualTo(BRAND);
        assertThat(price.productId()).isEqualTo(PRODUCT);
        assertThat(price.priceList()).isEqualTo(1);
        assertThat(price.startDate()).isEqualTo(LocalDateTime.of(2020, 6, 14, 0, 0));
        assertThat(price.endDate()).isEqualTo(LocalDateTime.of(2020, 12, 31, 23, 59, 59));
        assertThat(price.priority()).isZero();
        assertThat(price.price()).isEqualByComparingTo("10.55");
        assertThat(price.currency()).isEqualTo("EUR");
    }

    private static PriceQuery query() {
        return new PriceQuery(BRAND, PRODUCT, APPLICATION_DATE);
    }
}
