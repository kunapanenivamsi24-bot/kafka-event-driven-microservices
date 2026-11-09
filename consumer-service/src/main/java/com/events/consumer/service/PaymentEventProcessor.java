package com.events.consumer.service;

import com.events.common.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Business logic for a consumed payment event.
 *
 * <p>Validation failures throw {@link IllegalArgumentException} on purpose: the
 * listener's retry topic re-delivers the record a few times, and after the
 * retries are exhausted the record is routed to the dead-letter topic.
 */
@Service
public class PaymentEventProcessor {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProcessor.class);

    public void process(PaymentEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Payment event must not be null");
        }
        if (event.getAmount() == null || event.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Invalid payment amount for eventId=" + event.getEventId()
                            + " transactionId=" + event.getTransactionId()
                            + " amount=" + event.getAmount());
        }
        if (event.getTransactionId() == null || event.getTransactionId().isBlank()) {
            throw new IllegalArgumentException(
                    "Missing transactionId for eventId=" + event.getEventId());
        }

        // Demo business logic: in a real system this would post the payment to a
        // ledger, update balances, or call a downstream service.
        log.info("Processed payment eventId={} transactionId={} accountId={} amount={} {} type={}",
                event.getEventId(), event.getTransactionId(), event.getAccountId(),
                event.getAmount(), event.getCurrency(), event.getType());
    }
}
