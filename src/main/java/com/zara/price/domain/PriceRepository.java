package com.zara.price.domain;

import java.util.Optional;

public interface PriceRepository {

    /**
     * Returns the price that applies to the query: among the prices whose interval contains the
     * application date, the one with the highest priority, then the latest start date, then the
     * highest id. The selection is resolved by the storage so that only one row is transferred.
     */
    Optional<Price> findApplicablePrice(PriceQuery query);
}
