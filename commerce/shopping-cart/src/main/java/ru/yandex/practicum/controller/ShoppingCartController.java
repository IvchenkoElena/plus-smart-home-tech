package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.ShoppingCartOperations;
import ru.yandex.practicum.dto.shoppindCart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.shoppindCart.ShoppingCartDto;
import ru.yandex.practicum.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/shopping-cart")
public class ShoppingCartController implements ShoppingCartOperations {
    private final ShoppingCartService shoppingCartService;

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        log.info("GET /api/v1/shopping-cart - Получить актуальную корзину для пользователя {}", username);
        ShoppingCartDto response = shoppingCartService.getShoppingCart(username);
        log.info("Возвращаем корзину: {}", response);
        return response;
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Integer> products) {
        log.info("PUT /api/v1/shopping-cart - Добавить товар в корзину пользователя {}", username);
        ShoppingCartDto response = shoppingCartService.addProductToShoppingCart(username, products);
        log.info("Возвращаем корзину: {}", response);
        return response;
    }

    @Override
    public void deactivateCurrentShoppingCart(String username) {
        log.info("DELETE /api/v1/shopping-cart - Деактивация корзины товаров для пользователя {}", username);
        shoppingCartService.deactivateCurrentShoppingCart(username);
        log.info("Деактивировали корзину пользователя {}", username);
    }

    @Override
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> products) {
        log.info("POST /api/v1/shopping-cart/remove - Изменить состав товаров в корзине пользователя {}", username);
        ShoppingCartDto response = shoppingCartService.removeFromShoppingCart(username, products);
        log.info("Возвращаем корзину: {}", response);
        return response;
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        log.info("POST /api/v1/shopping-cart/change-quantity - Изменить количество товаров в корзине пользователя {}", username);
        ShoppingCartDto response = shoppingCartService.changeProductQuantity(username, request);
        log.info("Возвращаем корзину: {}", response);
        return response;
    }

    @Override
    public String getUsernameById(UUID cartId) {
        return shoppingCartService.getUsernameById(cartId);
    }
}
