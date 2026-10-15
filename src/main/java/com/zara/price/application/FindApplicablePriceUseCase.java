package com.zara.price.application;

import com.zara.price.domain.Price;
import com.zara.price.domain.PriceQuery;
import java.util.Optional;

public interface FindApplicablePriceUseCase {

    Optional<Price> findApplicablePrice(PriceQuery query);
}
