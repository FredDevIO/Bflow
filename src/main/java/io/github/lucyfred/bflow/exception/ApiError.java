package io.github.lucyfred.bflow.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        LocalDateTime timeStamp,
        Integer status,
        String error,
        List<String> messages
) {
}
