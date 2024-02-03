package com.rowland.engineering.ecommerce.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.NOT_FOUND)
@AllArgsConstructor
public class ProductNotFoundException extends RuntimeException{
    private Long productId;
}
