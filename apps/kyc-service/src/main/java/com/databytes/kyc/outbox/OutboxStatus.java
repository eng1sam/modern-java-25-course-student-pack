package com.databytes.kyc.outbox;

/** Delivery state of an outbox row as it is relayed to Kafka. */
public enum OutboxStatus {
    PENDING,
    PROCESSED,
    FAILED
}
