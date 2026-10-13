package com.zara.price.adapter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRICES")
class PriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "brand_id", nullable = false)
    private long brandId;

    @Column(name = "product_id", nullable = false)
    private long productId;

    @Column(name = "price_list", nullable = false)
    private int priceList;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private int priority;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "curr", nullable = false, length = 3)
    private String currency;

    protected PriceEntity() {
    }

    Long getId() {
        return id;
    }

    long getBrandId() {
        return brandId;
    }

    long getProductId() {
        return productId;
    }

    int getPriceList() {
        return priceList;
    }

    LocalDateTime getStartDate() {
        return startDate;
    }

    LocalDateTime getEndDate() {
        return endDate;
    }

    int getPriority() {
        return priority;
    }

    BigDecimal getPrice() {
        return price;
    }

    String getCurrency() {
        return currency;
    }
}
