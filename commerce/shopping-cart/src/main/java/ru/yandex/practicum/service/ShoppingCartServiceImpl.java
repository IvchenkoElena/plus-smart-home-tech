package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.api.WarehouseOperations;
import ru.yandex.practicum.dto.shoppindCart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.shoppindCart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.exception.DeactivateCartException;
import ru.yandex.practicum.exception.NoCartException;
import ru.yandex.practicum.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.mapper.CartMapper;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.repository.CartRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final WarehouseOperations warehouseClient;

    @Transactional(readOnly = true)
    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        validateUsername(username);
        log.info("Запрашиваем актуальную корзину для пользователя {}", username);
        ShoppingCart cart = getOrCreateShoppingCart(username);
        log.info("Получили корзину");
        return cartMapper.toCartDto(cart);
    }

    @Transactional
    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Integer> products) {
        log.info("Начал работать метод addProduct, на вход пришло username:{}, {}", username, products);
        validateUsername(username);
        log.info("Запрашиваем актуальную корзину для пользователя {}", username);
        ShoppingCart cart = getOrCreateShoppingCart(username);
        checkCartIsActive(cart);
        Map<UUID, Integer> oldProducts = cart.getProducts();
        oldProducts.putAll(products);
        cart.setProducts(oldProducts);
        log.info("Добавили продукты в корзину");

        //проверить товары на складе
        BookedProductsDto bookedProductsDto = warehouseClient.checkProductQuantityEnoughForShoppingCart(cartMapper.toCartDto(cart));
        log.info("Проверили наличие товаров на складе, параметры заказа: {}", bookedProductsDto);

        cartRepository.save(cart);
        log.info("Сохранили обновленную корзину");
        return cartMapper.toCartDto(cart);
    }

    @Transactional
    @Override
    public void deactivateCurrentShoppingCart(String username) {
        validateUsername(username);
        log.info("Запрашиваем актуальную корзину для пользователя {}", username);
        ShoppingCart cart = getOrCreateShoppingCart(username);
        checkCartIsActive(cart);
        cart.setActive(false);
        log.info("Деактивировали корзину");
        cartRepository.save(cart);
        log.info("Сохранили деактивированную корзину");
    }

    @Transactional
    @Override
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> products) {
        validateUsername(username);
        log.info("Запрашиваем актуальную корзину для пользователя {}", username);
        ShoppingCart cart = getOrCreateShoppingCart(username);
        checkCartIsActive(cart);
        Map<UUID, Integer> oldProducts = cart.getProducts();
        for (UUID idToRemove : products) {
            if (oldProducts.containsKey(idToRemove)) {
                oldProducts.remove(idToRemove);
            } else {
                throw new NoProductsInShoppingCartException("Такого продукта нет в корзине");
            }
        }
        cart.setProducts(oldProducts);
        log.info("Удалили продукты из корзины");
        cartRepository.save(cart);
        log.info("Сохранили обновленную корзину");
        return cartMapper.toCartDto(cart);
    }

    @Transactional
    @Override
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        validateUsername(username);
        log.info("Запрашиваем актуальную корзину для пользователя {}", username);
        ShoppingCart cart = getOrCreateShoppingCart(username);
        checkCartIsActive(cart);
        Map<UUID, Integer> oldProducts = cart.getProducts();
        if (oldProducts.containsKey(request.getProductId())) {
            oldProducts.put(request.getProductId(), request.getNewQuantity());
        } else {
            throw new NoProductsInShoppingCartException("Такого продукта нет в корзине");
        }
        cart.setProducts(oldProducts);
        log.info("Изменили количество продукта в корзине");

        //проверить товары на складе
        BookedProductsDto bookedProductsDto = warehouseClient.checkProductQuantityEnoughForShoppingCart(cartMapper.toCartDto(cart));
        log.info("Проверили наличие товаров на складе, параметры заказа: {}", bookedProductsDto);
        
        cartRepository.save(cart);
        log.info("Сохранили обновленную корзину");
        return cartMapper.toCartDto(cart);
    }

    @Override
    public String getUsernameById(UUID cartId) {
        ShoppingCart shoppingCart = cartRepository.findByCartId(cartId)
                .orElseThrow(() -> new NoCartException("Корзина с таким ID не существует: {}" + cartId));
        return shoppingCart.getUsername();
    }

    private void validateUsername(String username) {
        if (username.isBlank()) {
            throw new NotAuthorizedUserException(username);
        }
    }

    private ShoppingCart getOrCreateShoppingCart(String username) {
        return cartRepository.findByUsername(username)
                .orElseGet(() -> {
                    log.debug("Корзина для пользователя {} не найдена, создаем новую", username);
                    ShoppingCart newCart = new ShoppingCart();
                    newCart.setUsername(username);
                    return cartRepository.save(newCart);
                });
    }

    private void checkCartIsActive(ShoppingCart cart) {
        if(!cart.getActive()) {
            throw new DeactivateCartException("Корзина пользователя " + cart.getUsername() + " не активна");
        }
    }
}
