package com.events.producer.service;

import com.events.common.PaymentEvent;
import com.events.common.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Publishes payment events to the {@code payments} topic, keyed by transaction id
 * so all events for one transaction land on the same partition, in order.
 */
@Service
public class PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public PaymentEventPublisher(KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, PaymentEvent>> publish(PaymentEvent event) {
        log.info("Publishing payment event eventId={} transactionId={} amount={} {}",
                event.getEventId(), event.getTransactionId(), event.getAmount(), event.getCurrency());
        CompletableFuture<SendResult<String, PaymentEvent>> future =
                kafkaTemplate.send(Topics.PAYMENTS_TOPIC, event.getTransactionId(), event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish payment event eventId={} transactionId={}",
                        event.getEventId(), event.getTransactionId(), ex);
            } else {
                log.info("Published payment event eventId={} to partition={} offset={}",
                        event.getEventId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
        return future;
    }
}
