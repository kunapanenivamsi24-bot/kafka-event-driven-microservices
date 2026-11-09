package com.events.common;

/**
 * Kafka topic names shared by the producer and consumer services.
 */
public final class Topics {

    public static final String PAYMENTS_TOPIC = "payments";

    public static final String PAYMENTS_DLT = "payments-dlt";

    private Topics() {
        // constants only
    }
}
