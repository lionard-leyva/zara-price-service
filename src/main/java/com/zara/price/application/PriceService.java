package com.zara.price.application;

import com.zara.price.domain.Price;
import com.zara.price.domain.PriceRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class PriceService {

    private final PriceRepository priceRepository;

    public PriceService(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    public Optional<Price> findApplicablePrice(long brandId, long productId, LocalDateTime applicationDate) {
        return priceRepository.findApplicable(brandId, productId, applicationDate);
    }
}
