package ru.yandex.practicum.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.dto.shoppingStore.PageableDto;
import ru.yandex.practicum.dto.shoppingStore.ProductCategory;
import ru.yandex.practicum.dto.shoppingStore.ProductDto;
import ru.yandex.practicum.dto.shoppingStore.SetProductQuantityStateRequest;

import java.util.List;
import java.util.UUID;

public interface ShoppingStoreOperations {

    @GetMapping
    public List<ProductDto> getProducts(@RequestParam ProductCategory category, PageableDto pageable);

    @PutMapping
    public ProductDto createNewProduct(@RequestBody @Valid ProductDto productDto);

    @PostMapping
    public ProductDto updateProduct(@RequestBody @Valid ProductDto productDto);

    @PostMapping("/removeProductFromStore")
    public boolean removeProductFromStore(@RequestBody @NotNull UUID productId);

    @PostMapping("quantityState")
    public boolean setProductQuantityState(@RequestBody @Valid SetProductQuantityStateRequest setProductQuantityStateRequest);

    @GetMapping("{productId}")
    public ProductDto getProduct(@PathVariable @NotNull UUID productId);

}
