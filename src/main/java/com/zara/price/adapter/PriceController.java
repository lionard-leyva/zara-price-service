package com.zara.price.adapter;

import com.zara.price.application.PriceService;
import com.zara.price.domain.Price;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping
    ResponseEntity<PriceResponse> findApplicablePrice(
            @RequestParam long brandId,
            @RequestParam long productId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime applicationDate) {

        return priceService.findApplicablePrice(brandId, productId, applicationDate)
                .map(PriceResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    record PriceResponse(
            long productId,
            long brandId,
            int priceList,
            String startDate,
            String endDate,
            BigDecimal price,
            String currency) {

        private static final DateTimeFormatter FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        static PriceResponse from(Price price) {
            return new PriceResponse(
                    price.productId(),
                    price.brandId(),
                    price.priceList(),
                    price.startDate().format(FORMAT),
                    price.endDate().format(FORMAT),
                    price.price(),
                    price.currency());
        }
    }
}
