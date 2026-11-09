package com.events.producer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Incoming request to publish a payment event. Validated before any event is built.
 */
public record PaymentPublishRequest(
        @NotBlank(message = "transactionId must not be blank")
        String transactionId,

        @NotBlank(message = "accountId must not be blank")
        String accountId,

        @Positive(message = "amount must be positive")
        BigDecimal amount,

        @NotBlank(message = "currency must not be blank")
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter ISO code, e.g. USD")
        String currency) {
}
