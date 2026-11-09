package com.events.producer.service;

import com.events.common.PaymentEvent;
import com.events.common.Topics;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentEventPublisherTest {

    @Mock
    private KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @InjectMocks
    private PaymentEventPublisher publisher;

    @Test
    void publish_sendsEventToPaymentsTopicKeyedByTransactionId() {
        PaymentEvent event = PaymentEvent.initiated("txn-123", "acct-456",
                new BigDecimal("250.00"), "USD");

        SendResult<String, PaymentEvent> sendResult = new SendResult<>(
                null, new RecordMetadata(new TopicPartition(Topics.PAYMENTS_TOPIC, 0), 0, 0, 0L, 0, 0));
        CompletableFuture<SendResult<String, PaymentEvent>> future =
                CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(eq(Topics.PAYMENTS_TOPIC), eq("txn-123"), eq(event)))
                .thenReturn(future);

        CompletableFuture<SendResult<String, PaymentEvent>> result = publisher.publish(event);

        assertSame(future, result);
        ArgumentCaptor<PaymentEvent> eventCaptor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(kafkaTemplate).send(eq(Topics.PAYMENTS_TOPIC), eq("txn-123"), eventCaptor.capture());
        assertEquals("txn-123", eventCaptor.getValue().getTransactionId());
        assertEquals(new BigDecimal("250.00"), eventCaptor.getValue().getAmount());
        assertEquals("USD", eventCaptor.getValue().getCurrency());
    }
}
