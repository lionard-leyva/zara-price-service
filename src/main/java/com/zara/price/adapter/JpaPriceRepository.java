package com.zara.price.adapter;

import com.zara.price.domain.Price;
import com.zara.price.domain.PriceRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
class JpaPriceRepository implements PriceRepository {

    private final SpringDataPriceRepository springDataRepository;

    JpaPriceRepository(SpringDataPriceRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<Price> findApplicable(long brandId, long productId, LocalDateTime applicationDate) {
        return springDataRepository
                .findApplicable(brandId, productId, applicationDate, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(JpaPriceRepository::toDomain);
    }

    private static Price toDomain(PriceEntity entity) {
        return new Price(
                entity.getBrandId(),
                entity.getProductId(),
                entity.getPriceList(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getPriority(),
                entity.getPrice(),
                entity.getCurrency());
    }
}
