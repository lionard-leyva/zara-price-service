package com.zara.price.domain;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PriceRepository {

    Optional<Price> findApplicable(long brandId, long productId, LocalDateTime applicationDate);
}
