package ru.yandex.practicum.accounts.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.accounts.controller.dto.UserDto;
import ru.yandex.practicum.accounts.model.AccountEntity;
import ru.yandex.practicum.accounts.model.UserEntity;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = AccountMapper.class)
public interface UserMapper {

    @Mapping(target = "accounts", source = "accountEntities")
    UserDto toUserDto(UserEntity userEntity, List<AccountEntity> accountEntities);
}
