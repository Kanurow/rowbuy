package com.rowland.engineering.ecommerce.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientProductQuantityException extends RuntimeException {
    private Long productId;

    public InsufficientProductQuantityException(Long productId) {
        this.productId = productId;
    }

}

