package com.zara.price.adapter;

import com.zara.price.domain.Price;
import com.zara.price.domain.PriceQuery;
import com.zara.price.domain.PriceRepository;
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
    public List<Price> findCandidates(PriceQuery query) {
        return springDataRepository
                .findCandidates(query.brandId(), query.productId(), query.applicationDate())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
