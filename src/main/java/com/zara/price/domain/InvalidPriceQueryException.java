package com.zara.price.domain;

public class InvalidPriceQueryException extends RuntimeException {

    public InvalidPriceQueryException(String message) {
        super(message);
    }
}
