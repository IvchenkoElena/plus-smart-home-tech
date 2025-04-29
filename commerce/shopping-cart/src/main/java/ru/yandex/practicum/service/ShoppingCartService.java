package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.shoppindCart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.shoppindCart.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartService {
    ShoppingCartDto getShoppingCart(String username);

    ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Integer> products);

    void deactivateCurrentShoppingCart(String username);

    ShoppingCartDto removeFromShoppingCart(String username, List<UUID> products);

    ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request);

    String getUsernameById(UUID cartId);
}
