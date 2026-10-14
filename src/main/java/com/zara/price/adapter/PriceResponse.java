package com.zara.price.adapter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

record PriceResponse(
        long productId,
        long brandId,
        int priceList,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BigDecimal price,
        String currency) {
}
