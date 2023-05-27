package com.tomwey2.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class LogRaw2SshdConnector {
    private static final Logger logger = LoggerFactory.getLogger(LogRaw2SshdConnector.class);
    private volatile boolean keepConsuming = true;
    final private Producer<String, String> producer;
    final private SshdLogStateMachine stateMachine;

    private static Properties buildProperties() {
        Properties props = new Properties();
        props.put("application.id", "log-raw2sshd-converter");
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("input.topic.name", "log-raw-data");
        props.put("output.topic.name", "log-sshd-data");
        props.put("group.id", "LogRaw2SshdConnector");
        return props;
    }

    public LogRaw2SshdConnector() {
        this.producer = new KafkaProducer<>(buildProperties());
        this.stateMachine = new SshdLogStateMachine();
    }

    public static void main(String[] args) {
        LogRaw2SshdConnector logRaw2SshdConnector = new LogRaw2SshdConnector();
        logRaw2SshdConnector.consume(buildProperties());
        Runtime.getRuntime().addShutdownHook(new Thread(logRaw2SshdConnector::shutdown));
    }

    private void consume(Properties props) {

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(List.of(props.getProperty("input.topic.name")));

            while (keepConsuming) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(250));
                for (ConsumerRecord<String, String> record: records) {
                    SshdLogStateMachine.States state = stateMachine.transform(record.key(), record.value());
                    if (state == SshdLogStateMachine.States.finished) {
                        logger.info(SshdEventFactory.toJsonString(stateMachine.getSshdEvent()));
                        Future<RecordMetadata> metadata = produce(
                                props.getProperty("output.topic.name"),
                                record.key(),
                                SshdEventFactory.toJsonString(stateMachine.getSshdEvent()));
                        printMetadata(metadata);
                        stateMachine.reset();
                    }
                }
            }
        }
    }

    private Future<RecordMetadata> produce(final String topic, final String key, final String value) {
        final ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
        return producer.send(record);
    }

    private void printMetadata(final Future<RecordMetadata> metadata) {
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

    private void shutdown() {
        logger.info("shutdown");
        producer.close();
        keepConsuming = false;
    }

}
