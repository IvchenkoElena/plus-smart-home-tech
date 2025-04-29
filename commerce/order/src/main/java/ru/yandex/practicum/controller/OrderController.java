package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.OrderOperations;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.service.OrderService;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController implements OrderOperations {
    private final OrderService orderService;

    @Override
    public List<OrderDto> getClientOrders(String username) {
        log.info("GET /api/v1/order - Получить заказы пользователя: username={}",
                username);
        List<OrderDto> response = orderService.getClientOrders(username);
        log.info("Возвращаем список заказов размером: {}", response.size());
        log.info("Возвращаем заказы: {}", response);
        return response;
    }

    @Override
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        log.info("PUT /api/v1/order - Создать новый заказ в системе: request={}", request);
        OrderDto response = orderService.createNewOrder(request);
        log.info("Возвращаем заказ: {}", response);
        return response;
    }

    @Override
    public OrderDto calculateDeliveryCost(UUID orderId) {
        log.info("POST /api/v1/order/calculate/delivery - Расчёт стоимости доставки заказа: orderId={}", orderId);
        OrderDto response = orderService.calculateDeliveryCost(orderId);
        log.info("Возвращаем заказ: {}", response);
        return response;
    }

    @Override
    public OrderDto calculateTotalCost(UUID orderId) {
        log.info("POST /api/v1/order/calculate/total - Расчёт стоимости заказа: orderId={}", orderId);
        OrderDto response = orderService.calculateTotalCost(orderId);
        log.info("Возвращаем заказ: {}", response);
        return response;
    }

    @Override
    public OrderDto payment(UUID orderId) {
        log.info("POST /api/v1/order/payment - Оплата заказа: orderId={}", orderId);
        OrderDto response = orderService.payment(orderId);
        log.info("Возвращаем заказ: {}", response);
        return response;
    }

    @Override
    public OrderDto paymentFailed(UUID orderId) {
        log.info("POST /api/v1/order/payment/failed - Оплата заказа произошла с ошибкой: orderId={}", orderId);
        OrderDto response = orderService.paymentFailed(orderId);
        log.info("Возвращаем заказ: {}", response);
        return response;
    }

    @Override
    public OrderDto assembly(UUID orderId) {
        log.info("POST /api/v1/order/assembly - Сборка заказа: orderId={}", orderId);
        OrderDto response = orderService.assembly(orderId);
        log.info("Возвращаем заказ: {}", response);
        return response;
    }

    @Override
    public OrderDto assemblyFailed(UUID orderId) {
        log.info("POST /api/v1/order/assembly/failed - Сборка заказа произошла с ошибкой: orderId={}", orderId);
        OrderDto response = orderService.assemblyFailed(orderId);
        log.info("Возвращаем заказ: {}", response);
        return response;
    }

    @Override
    public OrderDto delivery(UUID orderId) {
        log.info("POST /api/v1/order/delivery - Доставка заказа: orderId={}", orderId);
        OrderDto response = orderService.delivery(orderId);
        log.info("Возвращаем заказ: {}", response);
        return response;
    }

    @Override
    public OrderDto deliveryFailed(UUID orderId) {
        log.info("POST /api/v1/order/delivery/failed - Доставка заказа произошла с ошибкой: orderId={}", orderId);
        OrderDto response = orderService.deliveryFailed(orderId);
        log.info("Возвращаем заказ: {}", response);
        return response;
    }

    @Override
    public OrderDto complete(UUID orderId) {
        log.info("POST /api/v1/order/completed - Завершение заказа: orderId={}", orderId);
        OrderDto response = orderService.complete(orderId);
        log.info("Возвращаем заказ: {}", response);
        return response;
    }

    @Override
    public OrderDto productReturn(ProductReturnRequest request) {
        log.info("POST /api/v1/order/return - Возврат заказа: request={}", request);
        OrderDto response = orderService.productReturn(request);
        log.info("Возвращаем заказ: {}", response);
        return response;
    }
}
