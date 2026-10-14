package com.zara.price.adapter;

import com.zara.price.domain.Price;
import com.zara.price.domain.PriceRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
class JpaPriceRepository implements PriceRepository {

    private final SpringDataPriceRepository springDataRepository;
    private final PriceEntityMapper mapper;

    JpaPriceRepository(SpringDataPriceRepository springDataRepository, PriceEntityMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Price> findCandidates(long brandId, long productId, LocalDateTime applicationDate) {
        return springDataRepository
                .findCandidates(brandId, productId, applicationDate)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
