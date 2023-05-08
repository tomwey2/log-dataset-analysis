package com.tomwey.kafka;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Producer for the raw log data
 *
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        final Properties props = KafkaProducerApplication.loadProperties("bin/getting-started.properties");
        final String topic = "log-raw-data";
        final String filePath = "log-raw-data-producer/Linux_2k.log";
        final long waitMillis = 100;

        final Producer<String, String> producer = new KafkaProducer<>(props);
        final KafkaProducerApplication producerApp = new KafkaProducerApplication(producer, topic, filePath);
        try {
            log.info("read {} and produce messages to kafka topic {}", filePath, topic);
            try (final FileReader fileReader = new FileReader(filePath)) {
                final var reader = new BufferedReader(fileReader);
                String message = "";
                while ((message = reader.readLine()) != null) {
                    if (filter(message)) {
                        Thread.sleep(waitMillis);
                        Future<RecordMetadata> metadata = producerApp.produce(message);
                        producerApp.printOneMetadata(metadata, filePath);
                    }
                }
            }
        } finally {
            producerApp.shutdown();
        }
    }

    private static boolean filter(final String line) {
        List<String> blacklist = List.of(
                "combo kernel", "combo sysctl", "combo hcid", "combo rc", "combo network", "combo sdpd",
                "combo bluetooth",
                "combo syslog", "combo irqbalance", "combo portmap", "combo nfslock", "combo rpcidmapd", "combo random",
                "combo rpc.statd");

        for (String word : blacklist) {
            if (line.contains(word)) {
                return false;
            }
        }
        return true;
    }

}
