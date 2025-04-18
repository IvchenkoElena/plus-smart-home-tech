package ru.yandex.practicum.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.model.WarehouseProduct;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface WarehouseMapper {

    @Mapping(target = "quantity", ignore = true)
    @Mapping(target = "depth", source = "dto.dimension.depth")
    @Mapping(target = "width", source = "dto.dimension.width")
    @Mapping(target = "height", source = "dto.dimension.height")
    WarehouseProduct toEntity(NewProductInWarehouseRequest dto);

}
