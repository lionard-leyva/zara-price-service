package com.zara.price.adapter;

import com.zara.price.domain.Price;
import com.zara.price.domain.PriceRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
class JpaPriceRepository implements PriceRepository {

    private final SpringDataPriceRepository springDataRepository;

    JpaPriceRepository(SpringDataPriceRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public List<Price> findCandidates(long brandId, long productId, LocalDateTime applicationDate) {
        return springDataRepository
                .findCandidates(brandId, productId, applicationDate)
                .stream()
                .map(JpaPriceRepository::toDomain)
                .toList();
    }

    private static Price toDomain(PriceEntity entity) {
        return new Price(
                entity.getId(),
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
