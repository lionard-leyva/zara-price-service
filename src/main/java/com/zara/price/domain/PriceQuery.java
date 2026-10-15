package com.zara.price.domain;

import java.time.LocalDateTime;

public record PriceQuery(Long brandId, Long productId, LocalDateTime applicationDate) {

    public PriceQuery {
        if (brandId == null) {
            throw new InvalidPriceQueryException("brandId is required");
        }
        if (brandId <= 0) {
            throw new InvalidPriceQueryException("brandId must be positive");
        }
        if (productId == null) {
            throw new InvalidPriceQueryException("productId is required");
        }
        if (productId <= 0) {
            throw new InvalidPriceQueryException("productId must be positive");
        }
        if (applicationDate == null) {
            throw new InvalidPriceQueryException("applicationDate is required");
        }
    }
}
