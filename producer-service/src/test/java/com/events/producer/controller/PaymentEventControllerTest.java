package com.events.producer.controller;

import com.events.common.PaymentEvent;
import com.events.producer.service.PaymentEventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentEventController.class)
class PaymentEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentEventPublisher publisher;

    @Test
    void publish_validRequest_returns202WithEventId() throws Exception {
        when(publisher.publish(any(PaymentEvent.class)))
                .thenReturn(CompletableFuture.<SendResult<String, PaymentEvent>>completedFuture(null));

        mockMvc.perform(post("/api/payments/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "transactionId": "txn-123",
                                  "accountId": "acct-456",
                                  "amount": 250.00,
                                  "currency": "USD"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.eventId", notNullValue()))
                .andExpect(jsonPath("$.transactionId").value("txn-123"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(publisher).publish(captor.capture());
        assertEquals("txn-123", captor.getValue().getTransactionId());
        assertEquals("acct-456", captor.getValue().getAccountId());
    }

    @Test
    void publish_invalidAmount_returns400() throws Exception {
        mockMvc.perform(post("/api/payments/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "transactionId": "txn-123",
                                  "accountId": "acct-456",
                                  "amount": -5.00,
                                  "currency": "USD"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publish_badCurrency_returns400() throws Exception {
        mockMvc.perform(post("/api/payments/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "transactionId": "txn-123",
                                  "accountId": "acct-456",
                                  "amount": 10.00,
                                  "currency": "US"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
