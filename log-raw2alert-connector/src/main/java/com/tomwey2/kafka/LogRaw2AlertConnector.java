package com.tomwey2.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Converter raw log data to alert messages
 */
public class LogRaw2AlertConnector {
    private static final Logger logger = LoggerFactory.getLogger(LogRaw2AlertConnector.class);

    private static Properties buildProperties() {
        Properties props = new Properties();
        props.put("application.id", "log-raw2alert-converter");
        props.put("bootstrap.servers", "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, String.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, String.class);
        props.put("schema.registry.url", "");
        props.put("input.topic.name", "log-raw-data");
        props.put("output.topic.name", "log-alert-data");

        return props;
    }

    private static void runKafkaStreams(final KafkaStreams streams) {
        final CountDownLatch latch = new CountDownLatch(1);
        streams.setStateListener((newState, oldState) -> {
            if (oldState == KafkaStreams.State.RUNNING && newState != KafkaStreams.State.RUNNING) {
                latch.countDown();
            }
        });

        streams.start();

        try {
            latch.await();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }

        logger.info("Streams Closed");
    }

    private static Topology buildTopology(String inputTopic, String outputTopic) {
        Serde<String> stringSerde = Serdes.String();
        ObjectMapper objectMapper = new ObjectMapper();

        StreamsBuilder builder = new StreamsBuilder();
        builder
                .stream(inputTopic, Consumed.with(stringSerde, stringSerde))
                .peek((k, v) -> logger.info("Observed event: {}", v))
                .filter((k, v) -> v.contains("ALERT") || v.contains("warning"))
                // .mapValues(s -> s.toUpperCase())
                .mapValues(AlertEventFactory::create)
                .mapValues(alertEvent -> {
                    try {
                        return objectMapper.writeValueAsString(alertEvent);
                    } catch (JsonProcessingException e) {
                        logger.error(e.getMessage());
                        return null;
                    }
                })
                .peek((k, v) -> logger.info("Transformed event: {}", v))
                .to(outputTopic, Produced.with(stringSerde, stringSerde));

        return builder.build();
    }

    public static void main(String[] args) throws Exception {

        Properties props = buildProperties();
        final String inputTopic = props.getProperty("input.topic.name");
        final String outputTopic = props.getProperty("output.topic.name");

        KafkaStreams kafkaStreams = new KafkaStreams(
                buildTopology(inputTopic, outputTopic), props);

        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));

        logger.info("Kafka Streams {} started", props.getProperty("application.id"));
        runKafkaStreams(kafkaStreams);

    }
}
