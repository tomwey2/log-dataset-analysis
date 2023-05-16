package com.tomwey2.kafka;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class KafkaProducerAdapter {
    private final Producer<String, String> producer;
    final String inFile;
    final String outTopic;

    public KafkaProducerAdapter(final Producer<String, String> producer, final String topic,
            final String filePath) {
        this.producer = producer;
        outTopic = topic;
        inFile = filePath;
    }

    public Future<RecordMetadata> produce(final String message) {
        final ProducerRecord<String, String> producerRecord = new ProducerRecord<>(outTopic, inFile, message);
        return producer.send(producerRecord);
    }

    public void shutdown() {
        producer.close();
    }

    public static Properties loadProperties(String fileName) throws IOException {
        final Properties envProps = new Properties();
        final FileInputStream input = new FileInputStream(fileName);
        envProps.load(input);
        input.close();

        return envProps;
    }

    public void printMetadata(final Future<RecordMetadata> metadata, final String fileName) {
        System.out.println("Offsets and timestamps committed in batch from " + fileName);
        try {
            final RecordMetadata recordMetadata = metadata.get();
            System.out.println(
                    "Record written to offset " + recordMetadata.offset() + " timestamp " + recordMetadata.timestamp());
        } catch (InterruptedException | ExecutionException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
