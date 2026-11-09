package com.events.consumer.listener;

import com.events.common.PaymentEvent;
import com.events.common.Topics;
import com.events.consumer.service.PaymentEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

/**
 * Kafka listener for the {@code payments} topic.
 *
 * <p>Transient failures are retried with exponential backoff on dedicated retry
 * topics; after 4 attempts the record is published to {@code payments-dlt}
 * and handled by {@link #handleDeadLetter(PaymentEvent)}.
 */
@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final PaymentEventProcessor processor;

    public PaymentEventListener(PaymentEventProcessor processor) {
        this.processor = processor;
    }

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            dltTopicSuffix = "-dlt",
            autoCreateTopics = "true")
    @KafkaListener(topics = Topics.PAYMENTS_TOPIC, groupId = "${spring.kafka.consumer.group-id:payment-consumers}")
    public void onPaymentEvent(PaymentEvent event) {
        log.info("Received payment event eventId={} transactionId={}",
                event.getEventId(), event.getTransactionId());
        processor.process(event);
    }

    @DltHandler
    public void handleDeadLetter(PaymentEvent event) {
        // In a real system this would alert on-call / persist the record for
        // manual replay. For the demo, log it loudly and move on.
        log.error("Dead-lettered payment event after retries exhausted: eventId={} "
                        + "transactionId={} accountId={} amount={} {} type={}",
                event.getEventId(), event.getTransactionId(), event.getAccountId(),
                event.getAmount(), event.getCurrency(), event.getType());
    }
}
