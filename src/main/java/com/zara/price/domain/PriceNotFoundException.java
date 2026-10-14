package com.zara.price.domain;

public class PriceNotFoundException extends RuntimeException {

    public PriceNotFoundException(PriceQuery query) {
        super("No price applies to brand %d and product %d at %s"
                .formatted(query.brandId(), query.productId(), query.applicationDate()));
    }
}
