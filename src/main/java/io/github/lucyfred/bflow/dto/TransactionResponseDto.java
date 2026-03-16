package io.github.lucyfred.bflow.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponseDto(
        Long id,
        BigDecimal amount,
        String description,
        LocalDate transactionDate
) {
}
