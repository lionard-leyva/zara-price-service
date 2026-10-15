package com.zara.price.adapter.out.persistence;

import com.zara.price.domain.Price;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
interface PriceEntityMapper {

    Price toDomain(PriceEntity entity);
}
