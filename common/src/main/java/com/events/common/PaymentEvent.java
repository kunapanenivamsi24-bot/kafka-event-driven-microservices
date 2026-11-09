package com.events.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable payment event carried on the {@code payments} Kafka topic.
 * Serialized to JSON with Jackson; {@link Instant} is written as ISO-8601.
 */
public final class PaymentEvent {

    private final String eventId;
    private final String transactionId;
    private final String accountId;
    private final BigDecimal amount;
    private final String currency;
    private final PaymentEventType type;
    private final Instant timestamp;

    @JsonCreator
    public PaymentEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("transactionId") String transactionId,
            @JsonProperty("accountId") String accountId,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("currency") String currency,
            @JsonProperty("type") PaymentEventType type,
            @JsonProperty("timestamp") Instant timestamp) {
        this.eventId = eventId;
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amount = amount;
        this.currency = currency;
        this.type = type;
        this.timestamp = timestamp;
    }

    /** Convenience factory for a newly initiated payment. */
    public static PaymentEvent initiated(String transactionId, String accountId,
                                         BigDecimal amount, String currency) {
        return new PaymentEvent(
                UUID.randomUUID().toString(),
                transactionId,
                accountId,
                amount,
                currency,
                PaymentEventType.PAYMENT_INITIATED,
                Instant.now());
    }

    public String getEventId() {
        return eventId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentEventType getType() {
        return type;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PaymentEvent that)) {
            return false;
        }
        return Objects.equals(eventId, that.eventId)
                && Objects.equals(transactionId, that.transactionId)
                && Objects.equals(accountId, that.accountId)
                && Objects.equals(amount, that.amount)
                && Objects.equals(currency, that.currency)
                && type == that.type
                && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, transactionId, accountId, amount, currency, type, timestamp);
    }

    @Override
    public String toString() {
        return "PaymentEvent{"
                + "eventId='" + eventId + '\''
                + ", transactionId='" + transactionId + '\''
                + ", accountId='" + accountId + '\''
                + ", amount=" + amount
                + ", currency='" + currency + '\''
                + ", type=" + type
                + ", timestamp=" + timestamp
                + '}';
    }
}
