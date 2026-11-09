package com.events.common;

/**
 * Lifecycle states of a payment as it moves through the event-driven pipeline.
 */
public enum PaymentEventType {
    PAYMENT_INITIATED,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED
}
