package com.zara.price.adapter.in.web;

import java.time.LocalDateTime;

record PriceRequest(Long brandId, Long productId, LocalDateTime applicationDate) {
}
