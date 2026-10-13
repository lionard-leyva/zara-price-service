package com.zara.price.domain;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceRepository {

    List<Price> findCandidates(long brandId, long productId, LocalDateTime applicationDate);
}
