package com.zara.price.adapter.out.persistence;

import com.zara.price.domain.Price;
import com.zara.price.domain.PriceQuery;
import com.zara.price.domain.PriceRepository;
import java.util.Optional;
import org.springframework.data.domain.Limit;
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
    public Optional<Price> findApplicablePrice(PriceQuery query) {
        return springDataRepository
                .findApplicable(query.brandId(), query.productId(), query.applicationDate(), Limit.of(1))
                .stream()
                .findFirst()
                .map(mapper::toDomain);
    }
}
