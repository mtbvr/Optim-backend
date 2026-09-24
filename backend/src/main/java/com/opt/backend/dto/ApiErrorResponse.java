package com.opt.backend.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        String code,
        Map<String, Object> params
) {
    public static ApiErrorResponse of(int status, String error, String message, String path, String code, Map<String, Object> params) {
        return new ApiErrorResponse(OffsetDateTime.now(), status, error, message, path, code, params);
    }
}
