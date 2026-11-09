package com.events.consumer.listener;

import com.events.common.PaymentEvent;
import com.events.consumer.service.PaymentEventProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventListenerTest {

    @Mock
    private PaymentEventProcessor processor;

    @InjectMocks
    private PaymentEventListener listener;

    @Test
    void onPaymentEvent_delegatesToProcessor() {
        PaymentEvent event = PaymentEvent.initiated("txn-123", "acct-456",
                new BigDecimal("250.00"), "USD");

        listener.onPaymentEvent(event);

        verify(processor).process(event);
    }

    @Test
    void onPaymentEvent_processorFailure_propagatesToTriggerRetry() {
        PaymentEvent event = PaymentEvent.initiated("txn-123", "acct-456",
                new BigDecimal("-1.00"), "USD");
        doThrow(new IllegalArgumentException("Invalid payment amount"))
                .when(processor).process(event);

        // The exception must bubble up: @RetryableTopic only retries on a thrown exception.
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> listener.onPaymentEvent(event));
    }

    @Test
    void handleDeadLetter_logsWithoutThrowing() {
        PaymentEvent event = PaymentEvent.initiated("txn-123", "acct-456",
                new BigDecimal("-1.00"), "USD");

        assertDoesNotThrow(() -> listener.handleDeadLetter(event));
    }
}
