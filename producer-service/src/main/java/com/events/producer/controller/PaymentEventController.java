package com.events.producer.controller;

import com.events.common.PaymentEvent;
import com.events.producer.dto.PaymentPublishRequest;
import com.events.producer.service.PaymentEventPublisher;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST entry point: accepts a payment, turns it into a {@link PaymentEvent},
 * and publishes it to Kafka. Publishing is asynchronous, hence 202 Accepted.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentEventController {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventController.class);

    private final PaymentEventPublisher publisher;

    public PaymentEventController(PaymentEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/publish")
    public ResponseEntity<Map<String, String>> publish(@Valid @RequestBody PaymentPublishRequest request) {
        PaymentEvent event = PaymentEvent.initiated(
                request.transactionId(),
                request.accountId(),
                request.amount(),
                request.currency());
        publisher.publish(event);
        log.info("Accepted publish request eventId={} transactionId={}",
                event.getEventId(), event.getTransactionId());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of(
                        "eventId", event.getEventId(),
                        "transactionId", event.getTransactionId(),
                        "status", "ACCEPTED"));
    }
}
