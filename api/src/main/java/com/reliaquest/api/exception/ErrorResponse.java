package com.reliaquest.api.exception;

import java.time.OffsetDateTime;

public record ErrorResponse(
        String message,
        String path,
        OffsetDateTime timestamp,
        Integer code) {
                
        }


