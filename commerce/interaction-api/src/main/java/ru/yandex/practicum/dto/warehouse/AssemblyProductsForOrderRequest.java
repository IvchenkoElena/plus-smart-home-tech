package ru.yandex.practicum.dto.warehouse;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AssemblyProductsForOrderRequest {
    @NotNull
    private Map<UUID, Integer> products;
    @NotNull
    private UUID orderId;
}
