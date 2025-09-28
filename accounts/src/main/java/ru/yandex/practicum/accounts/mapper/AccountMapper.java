package ru.yandex.practicum.accounts.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import ru.yandex.practicum.accounts.controller.dto.AccountDto;
import ru.yandex.practicum.accounts.controller.dto.CurrencyDto;
import ru.yandex.practicum.accounts.model.AccountEntity;

import java.util.List;

import static java.util.Objects.isNull;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AccountMapper {

    @Mapping(target = "currency", source = "currency", qualifiedByName = "stringToCurrencyDto")
    AccountDto toAccountDto(AccountEntity accountEntity);

    List<AccountDto> toAccountDtos(List<AccountEntity> accountEntities);

    @Named("stringToCurrencyDto")
    default CurrencyDto stringToCurrencyDto(String currency) {
        if (isNull(currency)) {
            return null;
        }
        return CurrencyDto.builder()
                .name(currency)
                .build();
    }
}
