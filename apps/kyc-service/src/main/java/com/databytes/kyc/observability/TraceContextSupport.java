package com.databytes.kyc.observability;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Bridges the synchronous request trace and the asynchronous outbox dispatch.
 *
 * <p>At registration time {@link #captureTraceParent()} serialises the active
 * span into a W3C {@code traceparent} string that is stored on the outbox row.
 * When the dispatcher later relays the event it calls {@link #restore(String)}
 * to rebuild that context and makes it current, so the Kafka producer span — and
 * therefore the Screening Service consumer span — joins the original trace.
 */
@ApplicationScoped
public class TraceContextSupport {

    private static final String TRACEPARENT = "traceparent";

    private static final TextMapSetter<Map<String, String>> SETTER =
            (carrier, key, value) -> {
                if (carrier != null) {
                    carrier.put(key, value);
                }
            };

    private static final TextMapGetter<Map<String, String>> GETTER =
            new TextMapGetter<>() {
                @Override
                public Iterable<String> keys(Map<String, String> carrier) {
                    return carrier.keySet();
                }

                @Override
                public String get(Map<String, String> carrier, String key) {
                    return carrier == null ? null : carrier.get(key);
                }
            };

    @Inject
    OpenTelemetry openTelemetry;

    /** The current trace as a {@code traceparent} header value, or {@code null} if untraced. */
    public String captureTraceParent() {
        Map<String, String> carrier = new HashMap<>();
        openTelemetry.getPropagators().getTextMapPropagator()
                .inject(Context.current(), carrier, SETTER);
        return carrier.get(TRACEPARENT);
    }

    /** Rebuild an OpenTelemetry {@link Context} from a stored {@code traceparent}. */
    public Context restore(String traceParent) {
        if (traceParent == null || traceParent.isBlank()) {
            return Context.current();
        }
        Map<String, String> carrier = Map.of(TRACEPARENT, traceParent);
        return openTelemetry.getPropagators().getTextMapPropagator()
                .extract(Context.current(), carrier, GETTER);
    }
}
