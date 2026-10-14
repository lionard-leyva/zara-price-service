package com.zara.price.adapter;

import com.zara.price.application.PriceService;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    ResponseEntity<PriceResponse> findApplicablePrice(
            @RequestParam long brandId,
            @RequestParam long productId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime applicationDate) {

        return priceService.findApplicablePrice(brandId, productId, applicationDate)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
