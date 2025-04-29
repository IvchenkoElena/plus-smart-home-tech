package ru.yandex.practicum.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.dto.order.OrderDto;
import ru.yandex.practicum.dto.payment.PaymentDto;
import ru.yandex.practicum.model.Payment;

import java.math.BigDecimal;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface PaymentMapper {
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "totalPayment", source = "orderDto.totalPrice")
    @Mapping(target = "deliveryTotal", source = "orderDto.deliveryPrice")
    @Mapping(target = "feeTotal", source = "feeTotal")
    @Mapping(target = "productTotal", source = "orderDto.productPrice")
    @Mapping(target = "paymentState", ignore = true)
    @Mapping(target = "orderId", source = "orderDto.orderId")
    Payment toPayment(OrderDto orderDto, BigDecimal feeTotal);

    PaymentDto toPaymentDto(Payment payment);
}
