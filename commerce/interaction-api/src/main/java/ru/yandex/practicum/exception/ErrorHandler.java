package ru.yandex.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({ProductNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(RuntimeException e) {
        log.warn("404 - NOT_FOUND");
        return new ApiError(HttpStatus.NOT_FOUND, "Product not found", e.getMessage());
    }

    @ExceptionHandler({NoCartException.class, NoProductsInShoppingCartException. class, SpecifiedProductAlreadyInWarehouseException.class, NoOrderFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(RuntimeException e) {
        log.warn("400 - BAD_REQUEST");
        return new ApiError(HttpStatus.BAD_REQUEST, "Bad request", e.getMessage());
    }

    @ExceptionHandler({ProductInShoppingCartLowQuantityInWarehouse.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleProductInShoppingCartLowQuantityInWarehouseException(RuntimeException e) {
        log.warn("400 - BAD_REQUEST");
        return new ApiError(HttpStatus.BAD_REQUEST, "ProductInShoppingCartLowQuantityInWarehouse", e.getMessage());
    }

    @ExceptionHandler({NoSpecifiedProductInWarehouseException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNoSpecifiedProductInWarehouseException(RuntimeException e) {
        log.warn("404 - NOT_FOUND");
        return new ApiError(HttpStatus.NOT_FOUND, "NoSpecifiedProductInWarehouseException", e.getMessage());
    }

    @ExceptionHandler({NotAuthorizedUserException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleUnauthorizedException(RuntimeException e) {
        log.warn("401 - Unauthorized");
        return new ApiError(HttpStatus.UNAUTHORIZED, "Unauthorized", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleInternalServerError(RuntimeException e) {
        log.warn("500 - INTERNAL_SERVER_ERROR");
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", e.getMessage());
    }
}
