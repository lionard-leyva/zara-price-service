package com.zara.price.adapter.in.web;

import com.zara.price.domain.Price;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface PriceResponseMapper {

    PriceResponse toResponse(Price price);
}
