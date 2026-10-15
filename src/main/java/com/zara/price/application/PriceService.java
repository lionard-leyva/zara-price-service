package com.zara.price.application;

import com.zara.price.domain.Price;
import com.zara.price.domain.PriceQuery;
import com.zara.price.domain.PriceRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PriceService implements FindApplicablePriceUseCase {

    private final PriceRepository priceRepository;

    public PriceService(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    @Override
    public Optional<Price> findApplicablePrice(PriceQuery query) {
        return priceRepository.findApplicablePrice(query);
    }
}
