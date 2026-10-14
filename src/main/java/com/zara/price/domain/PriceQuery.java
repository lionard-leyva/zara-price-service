package com.zara.price.domain;

import java.time.LocalDateTime;

public record PriceQuery(long brandId, long productId, LocalDateTime applicationDate) {

    public PriceQuery {
        if (brandId <= 0) {
            throw new InvalidPriceQueryException("brandId must be positive");
        }
        if (productId <= 0) {
            throw new InvalidPriceQueryException("productId must be positive");
        }
        if (applicationDate == null) {
            throw new InvalidPriceQueryException("applicationDate is required");
        }
    }
}
