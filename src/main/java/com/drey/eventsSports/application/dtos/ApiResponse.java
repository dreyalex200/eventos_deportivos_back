package com.drey.eventsSports.application.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        Boolean success,
        String status,
        String message,
        T data,
        ErrorDetails error,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        LocalDateTime timestamp,
        UUID requestId
) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
                true,
                "success",
                message,
                data,
                null,
                LocalDateTime.now(),
                UUID.randomUUID()
        );
    }

    public static <T> ApiResponse<T> error(String code, String details) {
        return new ApiResponse<>(
                false,
                "error",
                null,
                null,
                new ErrorDetails(code, details),
                LocalDateTime.now(),
                UUID.randomUUID()
        );
    }

    public static <T> ApiResponse<T> error(String message, String code, String details) {
        return new ApiResponse<>(
                false,
                "error",
                message,
                null,
                new ErrorDetails(code, details),
                LocalDateTime.now(),
                UUID.randomUUID()
        );
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorDetails(
            String code,
            String details
    ) {
    }
}
