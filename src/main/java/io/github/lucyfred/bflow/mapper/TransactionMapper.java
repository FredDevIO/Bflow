package io.github.lucyfred.bflow.mapper;

import io.github.lucyfred.bflow.dto.TransactionRequestDto;
import io.github.lucyfred.bflow.dto.TransactionResponseDto;
import io.github.lucyfred.bflow.entity.Transaction;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    Transaction toTransactionFromRequest(TransactionRequestDto transactionRequestDto);
    Transaction toTransactionFromResponse(TransactionResponseDto transactionResponseDto);
    TransactionResponseDto toTransactionResponseDto(Transaction transaction);
    List<TransactionResponseDto> toListTransactionResponseDto(List<Transaction> transactions);
}
