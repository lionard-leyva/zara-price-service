package com.zara.price.adapter;

import com.zara.price.application.PriceService;
import com.zara.price.domain.PriceNotFoundException;
import com.zara.price.domain.PriceQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/prices")
class PriceController {

    private final PriceService priceService;
    private final PriceResponseMapper mapper;

    PriceController(PriceService priceService, PriceResponseMapper mapper) {
        this.priceService = priceService;
        this.mapper = mapper;
    }

    @GetMapping
    PriceResponse findApplicablePrice(@ModelAttribute PriceQuery query) {
        return priceService.findApplicablePrice(query)
                .map(mapper::toResponse)
                .orElseThrow(() -> new PriceNotFoundException(query));
    }
}
