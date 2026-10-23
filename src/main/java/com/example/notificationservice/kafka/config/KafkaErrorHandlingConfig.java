package com.example.notificationservice.kafka.config;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

/**
 * Failed records are retried, then published to {@code <topic>-dlt}.
 * Spring Boot wires this {@link DefaultErrorHandler} bean into the auto-configured listener container factory.
 */
@Configuration
public class KafkaErrorHandlingConfig {

    private static final String DLT_SUFFIX = "-dlt";

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaProperties kafkaProperties) {
        // deserialization failures arrive as the original raw bytes, listener failures as the deserialized event.
        // The JSON serializer adds no type info: the original record's headers (including the logical __TypeId__)
        // are copied to the DLT record, so don't overwrite them with this service's class names
        Map<Class<?>, Serializer<?>> valueSerializers = Map.of(
                byte[].class, new ByteArraySerializer(),
                Object.class, new JacksonJsonSerializer<>().noTypeInfo());

        KafkaTemplate<String, Object> deadLetterTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(
                kafkaProperties.buildProducerProperties(),
                new StringSerializer(),
                new DelegatingByTypeSerializer(valueSerializers, true)));

        // assignable = true: Object.class matches any event type; the most specific delegate (byte[]) is tried first
        // partition -1 lets Kafka choose, so the DLT doesn't need as many partitions as the source topic
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(deadLetterTemplate,
                (record, ex) -> new TopicPartition(record.topic() + DLT_SUFFIX, -1));

        // 2 retries 1s apart; deserialization failures are not retryable and go straight to the DLT
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));
    }
}
