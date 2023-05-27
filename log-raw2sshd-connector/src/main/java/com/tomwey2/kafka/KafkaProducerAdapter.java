package com.tomwey2.kafka;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class KafkaProducerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerAdapter.class);

    private final Producer<String, String> producer;
    final Properties props;

    public KafkaProducerAdapter(final Producer<String, String> producer, final Properties props) {
        this.producer = producer;
        this.props = props;
    }

    public Future<RecordMetadata> produce(final String topic, final String key, final String value) {
        final ProducerRecord<String, String> producerRecord =
                new ProducerRecord<>(topic, key, value);
        return producer.send(producerRecord);
    }

    public void shutdown() {
        logger.info("shutdown producer");
        producer.close();
    }

    public void printMetadata(final Future<RecordMetadata> metadata) {
        try {
            final RecordMetadata recordMetadata = metadata.get();
            logger.info("Record written to offset {} timestamp {}",
                    recordMetadata.offset(), recordMetadata.timestamp());
        } catch (InterruptedException | ExecutionException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
