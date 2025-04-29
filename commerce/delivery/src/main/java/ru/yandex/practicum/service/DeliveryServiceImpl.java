package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.api.OrderOperations;
import ru.yandex.practicum.api.WarehouseOperations;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.DeliveryState;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.warehouse.ShippedToDeliveryRequest;
import ru.yandex.practicum.exception.NoDeliveryFoundException;
import ru.yandex.practicum.mapper.DeliveryMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.Delivery;
import ru.yandex.practicum.repository.DeliveryRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;
    private final WarehouseOperations warehouseClient;
    private final OrderOperations orderClient;

    private static final BigDecimal BASE_RATE = BigDecimal.valueOf(5.0);
    private static final BigDecimal WAREHOUSE_1_ADDRESS_MULTIPLIER = BigDecimal.valueOf(1);
    private static final BigDecimal WAREHOUSE_2_ADDRESS_MULTIPLIER = BigDecimal.valueOf(2);
    private static final BigDecimal FRAGILE_MULTIPLIER = BigDecimal.valueOf(0.2);
    private static final BigDecimal WEIGHT_MULTIPLIER = BigDecimal.valueOf(0.3);
    private static final BigDecimal VOLUME_MULTIPLIER = BigDecimal.valueOf(0.2);
    private static final BigDecimal STREET_MULTIPLIER = BigDecimal.valueOf(0.2);

    @Transactional
    @Override
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        log.info("Создаем новую доставку в БД: deliveryDto={}", deliveryDto);
        Delivery delivery = deliveryMapper.toDelivery(deliveryDto);
        delivery = deliveryRepository.save(delivery);
        log.info("Возвращаем доставку с присвоенным идентификатором: {}", delivery);
        return deliveryMapper.toDeliveryDto(delivery);
    }

    @Transactional
    @Override
    public BigDecimal deliveryCost(OrderDto orderDto) {
        log.info("Рассчитываем стоимость доставки");
        Delivery delivery = deliveryRepository.findById(orderDto.getDeliveryId())
                .orElseThrow(() -> new NoDeliveryFoundException
                        ("Такой доставки не найдено: deliveryId = " + orderDto.getDeliveryId()));
        Address warehouseAddress = delivery.getFromAddress();
        Address destinationAddress = delivery.getToAddress();
        BigDecimal totalCost = BASE_RATE;
        totalCost = warehouseAddress.getCity().equals("ADDRESS_1") ?
                totalCost.add(totalCost.multiply(WAREHOUSE_1_ADDRESS_MULTIPLIER)) :
                totalCost.add(totalCost.multiply(WAREHOUSE_2_ADDRESS_MULTIPLIER));
        totalCost = orderDto.getFragile() == true ? totalCost.add(totalCost.multiply(FRAGILE_MULTIPLIER)) : totalCost;
        totalCost = totalCost.add(BigDecimal.valueOf(orderDto.getDeliveryWeight()).multiply(WEIGHT_MULTIPLIER));
        totalCost = totalCost.add(BigDecimal.valueOf(orderDto.getDeliveryVolume()).multiply(VOLUME_MULTIPLIER));
        totalCost = warehouseAddress.getStreet().equals(destinationAddress.getStreet()) ?
                totalCost : totalCost.add(totalCost.multiply(STREET_MULTIPLIER));
        log.info("Возвращаем стоимость доставки: {}", totalCost);
        return totalCost;
    }

    @Transactional
    @Override
    public void deliveryPicked(UUID orderId) {
        log.info("Передаем товар в доставку: orderId={}", orderId);
        // Изменить статус доставки на IN_PROGRESS
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException
                        ("Доставки для такого заказа не найдено: orderId = " + orderId));
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        delivery = deliveryRepository.save(delivery);
        // изменить статус заказа на ASSEMBLED в сервисе заказов
        orderClient.assembly(orderId);
        // и связать идентификатор доставки с внутренней учётной системой через вызов соответствующего метода склада
        warehouseClient.shippedToDelivery(new ShippedToDeliveryRequest(orderId, delivery.getDeliveryId()));
        log.info("Товар передан в доставку: orderId={}", orderId);
    }

    @Transactional
    @Override
    public void deliverySuccessful(UUID orderId) {
        log.info("Проставить признак успешной доставки товара: orderId={}", orderId);
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException
                        ("Доставки для такого заказа не найдено: orderId = " + orderId));
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);
        orderClient.delivery(orderId);
        log.info("Успешная доставка товара: orderId={}", orderId);
    }

    @Transactional
    @Override
    public void deliveryFailed(UUID orderId) {
        log.info("Проставить признак успешной доставки товара: orderId={}", orderId);
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException
                        ("Доставки для такого заказа не найдено: orderId = " + orderId));
        delivery.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);
        orderClient.deliveryFailed(orderId);
        log.info("Успешная доставка товара: orderId={}", orderId);
    }
}
