package com.events.consumer.service;

import com.events.common.PaymentEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentEventProcessorTest {

    private final PaymentEventProcessor processor = new PaymentEventProcessor();

    @Test
    void process_validEvent_doesNotThrow() {
        PaymentEvent event = PaymentEvent.initiated("txn-123", "acct-456",
                new BigDecimal("250.00"), "USD");
        assertDoesNotThrow(() -> processor.process(event));
    }

    @Test
    void process_zeroAmount_throwsIllegalArgumentException() {
        PaymentEvent event = PaymentEvent.initiated("txn-123", "acct-456",
                BigDecimal.ZERO, "USD");
        assertThrows(IllegalArgumentException.class, () -> processor.process(event));
    }

    @Test
    void process_negativeAmount_throwsIllegalArgumentException() {
        PaymentEvent event = PaymentEvent.initiated("txn-123", "acct-456",
                new BigDecimal("-5.00"), "USD");
        assertThrows(IllegalArgumentException.class, () -> processor.process(event));
    }

    @Test
    void process_nullEvent_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> processor.process(null));
    }
}
