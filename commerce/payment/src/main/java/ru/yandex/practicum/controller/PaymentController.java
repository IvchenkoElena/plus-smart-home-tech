package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.PaymentOperations;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
public class PaymentController implements PaymentOperations {
    private final PaymentService paymentService;

    @Override
    public BigDecimal productCost(OrderDto orderDto) {
        log.info("POST /api/v1/payment/productCost - Расчёт стоимости товаров в заказе: orderDto={}", orderDto);
        BigDecimal response = paymentService.productCost(orderDto);
        log.info("Стоимость товаров в заказе: {}", response);
        return response;
    }

    @Override
    public BigDecimal getTotalCost(OrderDto orderDto) {
        log.info("POST /api/v1/payment/totalCost - Расчёт полной стоимости заказа: orderDto={}", orderDto);
        BigDecimal response = paymentService.getTotalCost(orderDto);
        log.info("Полная стоимость заказа: {}", response);
        return response;
    }

    @Override
    public PaymentDto payment(OrderDto orderDto) {
        log.info("POST /api/v1/payment - Формирование оплаты для заказа: orderDto={}", orderDto);
        PaymentDto response = paymentService.payment(orderDto);
        log.info("Сформированная оплата заказа: {}", response);
        return response;
    }

    @Override
    public void paymentSuccess(UUID paymentId) {
        log.info("POST /api/v1/payment/refund - Метод для эмуляции успешной оплаты платежного шлюза: paymentId={}", paymentId);
        paymentService.paymentSuccess(paymentId);
        log.info("Успешная оплата заказа");
    }

    @Override
    public void paymentFailed(UUID paymentId) {
        log.info("POST api/v1/payment/failed - Метод для эмуляции отказа в оплате платежного шлюза: paymentId={}", paymentId);
        paymentService.paymentFailed(paymentId);
        log.info("Отказ в оплате заказа");
    }
}
