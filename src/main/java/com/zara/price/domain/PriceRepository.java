package com.zara.price.domain;

import java.util.List;

public interface PriceRepository {

    List<Price> findCandidates(PriceQuery query);
}
