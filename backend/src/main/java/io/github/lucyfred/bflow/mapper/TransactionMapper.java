package io.github.lucyfred.bflow.mapper;

import io.github.lucyfred.bflow.dto.TransactionRequestDto;
import io.github.lucyfred.bflow.dto.TransactionResponseDto;
import io.github.lucyfred.bflow.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    Transaction toTransactionFromRequest(TransactionRequestDto transactionRequestDto);
    Transaction toTransactionFromResponse(TransactionResponseDto transactionResponseDto);
    @Mapping(source = "category.type", target = "categoryType")
    @Mapping(source = "category.name", target = "categoryName")
    TransactionResponseDto toTransactionResponseDto(Transaction transaction);
    List<TransactionResponseDto> toListTransactionResponseDto(List<Transaction> transactions);
}
