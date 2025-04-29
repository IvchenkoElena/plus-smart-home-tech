package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.WarehouseOperations;
import ru.yandex.practicum.dto.shoppindCart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.ShippedToDeliveryRequest;
import ru.yandex.practicum.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/warehouse")
public class WarehouseController implements WarehouseOperations {
    private final WarehouseService warehouseService;

    @Override
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        log.info("PUT /api/v1/warehouse - Добавить новый товар на склад {}", request);
        warehouseService.newProductInWarehouse(request);
        log.info("Товар добавлен на склад", request);
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto cartDto) {
        log.info("POST /api/v1/warehouse/check - Проверка количества товаров на складе: {}", cartDto);
        BookedProductsDto response = warehouseService.checkProductQuantityEnoughForShoppingCart(cartDto);
        log.info("Товары зарезервированы: {}", response);
        return response;
    }

    @Override
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        log.info("POST /api/v1/warehouse/add - Принять товар на склад: {}", request);
        warehouseService.addProductToWarehouse(request);
        log.info("Товар принят на склад: {}", request);
    }

    @Override
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        log.info("POST /api/v1/warehouse/assembly - Собрать товары products{} к заказу orderId={} для подготовки к отправке", request.getProducts(), request.getOrderId());
        warehouseService.assemblyProductsForOrder(request);
        log.info("Товары products{} к заказу orderId={} собраны для подготовки к отправке", request.getProducts(), request.getOrderId());
        return null;
    }

    @Override
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        log.info("POST /api/v1/warehouse/shipped - Передать товары в доставку: {}", request);
        warehouseService.shippedToDelivery(request);
        log.info("Товары переданы на доставку: {}", request);
    }

    @Override
    public void acceptReturn(Map<UUID, Integer> productsToReturn) {
        log.info("POST /api/v1/warehouse/return - Принять возврат товаров на склад: {}", productsToReturn);
        warehouseService.acceptReturn(productsToReturn);
        log.info("Товар принят на склад: {}", productsToReturn);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        log.info("GET /api/v1/warehouse/address - Предоставить адрес склада для расчёта доставки");
        AddressDto response = warehouseService.getWarehouseAddress();
        log.info("Адрес предоставлен: {}", response);
        return response;
    }
}
