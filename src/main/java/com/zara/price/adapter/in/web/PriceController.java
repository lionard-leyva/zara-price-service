package com.zara.price.adapter.in.web;

import com.zara.price.application.FindApplicablePriceUseCase;
import com.zara.price.domain.PriceNotFoundException;
import com.zara.price.domain.PriceQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/prices")
class PriceController {

    private final FindApplicablePriceUseCase findApplicablePrice;
    private final PriceResponseMapper mapper;

    PriceController(FindApplicablePriceUseCase findApplicablePrice, PriceResponseMapper mapper) {
        this.findApplicablePrice = findApplicablePrice;
        this.mapper = mapper;
    }

    @GetMapping
    PriceResponse findApplicablePrice(@ModelAttribute PriceRequest request) {
        PriceQuery query = new PriceQuery(request.brandId(), request.productId(), request.applicationDate());
        return findApplicablePrice.findApplicablePrice(query)
                .map(mapper::toResponse)
                .orElseThrow(() -> new PriceNotFoundException(query));
    }
}
