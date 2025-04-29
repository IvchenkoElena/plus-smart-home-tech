package ru.yandex.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.api.DeliveryOperations;
import ru.yandex.practicum.api.PaymentOperations;
import ru.yandex.practicum.api.ShoppingCartOperations;
import ru.yandex.practicum.api.WarehouseOperations;
import ru.yandex.practicum.dto.delivery.DeliveryDto;
import ru.yandex.practicum.dto.delivery.DeliveryState;
import ru.yandex.practicum.dto.order.CreateNewOrderRequest;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.order.OrderState;
import ru.yandex.practicum.dto.order.ProductReturnRequest;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.exception.NoCartException;
import ru.yandex.practicum.exception.NoOrderFoundException;
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.Order;
import ru.yandex.practicum.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final WarehouseOperations warehouseClient;
    private final ShoppingCartOperations shoppingCartClient;
    private final PaymentOperations paymentClient;
    private final DeliveryOperations deliveryClient;

    @Transactional(readOnly = true)
    @Override
    public List<OrderDto> getClientOrders(String username) {
        validateUsername(username);
        log.info("Запрашиваем список всех заказов пользователя {}", username);
        List<Order> orders = orderRepository.findAllByUsername(username);
        log.debug("Получили из DB список заказов размером {}", orders.size());
        return orders.stream().
                map(orderMapper::toOrderDto).
                toList();
    }

    @Transactional
    @Override
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        log.info("Создаем новый заказ: shoppingCartId {}, products {}", request.getShoppingCart().getCartId(), request.getShoppingCart().getProducts());
        BookedProductsDto bookedProductsDto;
        try {
            bookedProductsDto = warehouseClient.checkProductQuantityEnoughForShoppingCart(request.getShoppingCart());
            log.info("Проверили наличие товаров на складе, параметры заказа: {}", bookedProductsDto);
        } catch (FeignException e) {
            if (e.status() == 400) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(e.getMessage());
            } else if (e.status() == 404) {
                throw new NoSpecifiedProductInWarehouseException(e.getMessage());
            } else {
                throw new RuntimeException(e.getMessage());
            }
        }
        // попыталась разобраться с настройкой curquit breaker.
        // видимо в нашем случае нужно использовать fallback factory,
        // чтобы по-разщному обрабатывать разные ошибки из feign клиентов,
        // но не нашла, где бы было понятно описано, как именно это реализовать

        String username;
        try {
            username = shoppingCartClient.getUsernameById(request.getShoppingCart().getCartId());
            log.info("Нашли имя пользователя {}", username);
        } catch (FeignException e) {
            if (e.status() == 400) {
                throw new NoCartException(e.getMessage());
            } else {
                throw new RuntimeException(e.getMessage());
            }
        }
        Order newOrder = orderMapper.toOrder(request, bookedProductsDto, username);
        newOrder = orderRepository.save(newOrder);

        // Задаем DeliveryId

        DeliveryDto deliveryDto = new DeliveryDto();
        deliveryDto.setFromAddress(warehouseClient.getWarehouseAddress());
        deliveryDto.setToAddress(request.getDeliveryAddress());
        deliveryDto.setOrderId(newOrder.getOrderId());
        deliveryDto.setDeliveryState(DeliveryState.CREATED);

        DeliveryDto newDeliveryDto = deliveryClient.planDelivery(deliveryDto);

        newOrder.setDeliveryId(newDeliveryDto.getDeliveryId());
        newOrder = orderRepository.save(newOrder);
        log.info("Сохранили новый заказ в БД: {}", newOrder);
        return orderMapper.toOrderDto(newOrder);
    }

    @Transactional
    @Override
    public OrderDto calculateDeliveryCost(UUID orderId) {
        log.info("Обрабатываем вычисление стоимости доставки заказа OrderId: {}", orderId);
        Order orderToCalculate = getOrderById(orderId);
        //вычислить и задать поля
        BigDecimal productCost = paymentClient.productCost(orderMapper.toOrderDto(orderToCalculate));
        orderToCalculate.setProductPrice(productCost);
        BigDecimal deliveryCost = deliveryClient.deliveryCost(orderMapper.toOrderDto(orderToCalculate));
        orderToCalculate.setDeliveryPrice(deliveryCost);

        orderToCalculate = orderRepository.save(orderToCalculate);
        log.info("Сохранили изменения в БД: {}", orderToCalculate);
        return orderMapper.toOrderDto(orderToCalculate);
    }

    @Transactional
    @Override
    public OrderDto calculateTotalCost(UUID orderId) {
        log.info("Обрабатываем вычисление общей стоимости заказа OrderId: {}", orderId);
        Order orderToCalculate = getOrderById(orderId);
        //вычислить и задать поле
        BigDecimal totalCost = paymentClient.getTotalCost(orderMapper.toOrderDto(orderToCalculate));
        orderToCalculate.setTotalPrice(totalCost);

        PaymentDto paymentDto =  paymentClient.payment(orderMapper.toOrderDto(orderToCalculate));
        orderToCalculate.setPaymentId(paymentDto.getPaymentId());

        orderToCalculate = orderRepository.save(orderToCalculate);
        log.info("Сохранили изменения в БД: {}", orderToCalculate);
        return orderMapper.toOrderDto(orderToCalculate);
    }

    @Transactional
    @Override
    public OrderDto payment(UUID orderId) {
        log.info("Обрабатываем успешный платеж по заказу OrderId: {}", orderId);

        Order orderToPay = getOrderById(orderId);
        orderToPay = changeOrderStateAndSave(orderToPay, OrderState.PAID);

        warehouseClient.assemblyProductsForOrder(new AssemblyProductsForOrderRequest(orderToPay.getProducts(), orderId));

        return orderMapper.toOrderDto(orderToPay);
    }

    @Transactional
    @Override
    public OrderDto paymentFailed(UUID orderId) {
        log.info("Обрабатываем ошибку оплаты заказа OrderId: {}", orderId);
        Order orderToPay = getOrderById(orderId);
        orderToPay = changeOrderStateAndSave(orderToPay, OrderState.PAYMENT_FAILED);
        return orderMapper.toOrderDto(orderToPay);
    }

    @Transactional
    @Override
    public OrderDto assembly(UUID orderId) {
        log.info("Обрабатываем успешную сборку заказа OrderId: {}", orderId);
        Order orderToAssembly = getOrderById(orderId);
        orderToAssembly = changeOrderStateAndSave(orderToAssembly, OrderState.ASSEMBLED);
        return orderMapper.toOrderDto(orderToAssembly);
    }

    @Transactional
    @Override
    public OrderDto assemblyFailed(UUID orderId) {
        log.info("Обрабатываем ошибку сборки по заказу OrderId: {}", orderId);
        Order orderToAssembly = getOrderById(orderId);
        orderToAssembly = changeOrderStateAndSave(orderToAssembly, OrderState.ASSEMBLY_FAILED);
        return orderMapper.toOrderDto(orderToAssembly);
    }

    @Transactional
    @Override
    public OrderDto delivery(UUID orderId) {
        log.info("Обрабатываем успешную доставку по заказу OrderId: {}", orderId);
        Order orderToDeliver = getOrderById(orderId);
        orderToDeliver = changeOrderStateAndSave(orderToDeliver, OrderState.DELIVERED);
        return orderMapper.toOrderDto(orderToDeliver);
    }

    @Transactional
    @Override
    public OrderDto deliveryFailed(UUID orderId) {
        log.info("Обрабатываем ошибку доставки заказа OrderId: {}", orderId);
        Order orderToDeliver = getOrderById(orderId);
        orderToDeliver = changeOrderStateAndSave(orderToDeliver, OrderState.DELIVERY_FAILED);
        return orderMapper.toOrderDto(orderToDeliver);
    }

    @Transactional
    @Override
    public OrderDto complete(UUID orderId) {
        log.info("Обрабатываем завершение заказа OrderId: {}", orderId);
        Order orderToComplete = getOrderById(orderId);
        orderToComplete = changeOrderStateAndSave(orderToComplete, OrderState.COMPLETED);
        return orderMapper.toOrderDto(orderToComplete);
    }

    @Transactional
    @Override
    public OrderDto productReturn(ProductReturnRequest request) {
        log.info("Создан запрос на возврат заказа OrderId: {}, products: {}", request.getOrderId(), request.getProducts());
        Order orderToReturn = getOrderById(request.getOrderId());
        Map<UUID, Integer> productsToReturn = request.getProducts();
        Set<UUID> ids = productsToReturn.keySet();
        for (UUID id : ids) {
            AddProductToWarehouseRequest addProductToWarehouseRequest = new AddProductToWarehouseRequest(id, productsToReturn.get(id));
            warehouseClient.addProductToWarehouse(addProductToWarehouseRequest);
        }
        log.info("Вернули на склад товары из заказа: OrderId {}, products {}", request.getOrderId(), request.getProducts());
        orderToReturn = changeOrderStateAndSave(orderToReturn, OrderState.PRODUCT_RETURNED);
        return orderMapper.toOrderDto(orderToReturn);
    }

    private void validateUsername(String username) {
        if (username.isBlank()) {
            throw new NotAuthorizedUserException(username);
        }
    }

    private Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Такого заказа нет в базе: " + orderId));
    }

    private Order changeOrderStateAndSave(Order order, OrderState orderState) {
        order.setOrderState(orderState);
        log.info("Изменили статус заказа на {}", orderState);
        order = orderRepository.save(order);
        log.info("Сохранили изменения в БД: {}", order);
        return order;
    }
}
