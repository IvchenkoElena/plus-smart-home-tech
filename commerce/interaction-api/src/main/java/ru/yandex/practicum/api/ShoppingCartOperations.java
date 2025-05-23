package ru.yandex.practicum.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.dto.shoppindCart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.shoppindCart.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "shopping-cart", path = "/api/v1/shopping-cart")
public interface ShoppingCartOperations {
    @GetMapping
    public ShoppingCartDto getShoppingCart(@RequestParam(name = "username") @NotNull String username);

    @PutMapping
    public ShoppingCartDto addProductToShoppingCart(@RequestParam(name = "username") @NotNull String username,
                                                    @RequestBody Map<UUID, Integer> products);

    @DeleteMapping
    public void deactivateCurrentShoppingCart(@RequestParam(name = "username") @NotNull String username);

    @PostMapping("/remove")
    public ShoppingCartDto removeFromShoppingCart(@RequestParam(name = "username") @NotNull String username,
                                                  @RequestBody List<UUID> products);

    @PostMapping("/change-quantity")
    public ShoppingCartDto changeProductQuantity(@RequestParam(name = "username") @NotNull String username,
                                                 @RequestBody @Valid ChangeProductQuantityRequest request);
}
