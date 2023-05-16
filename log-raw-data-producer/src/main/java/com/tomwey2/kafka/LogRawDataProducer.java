package com.tomwey2.kafka;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Producer for the raw log data
 *
 */
public class LogRawDataProducer {
    private static final Logger log = LoggerFactory.getLogger(LogRawDataProducer.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        final Properties props = KafkaProducerAdapter.loadProperties("bin/getting-started.properties");
        final String topic = "log-raw-data";
        final String filePath = "log-raw-data-producer/Linux_2k.log";
        final long waitMillis = 100;

        final Producer<String, String> producer = new KafkaProducer<>(props);
        final KafkaProducerAdapter producerApp = new KafkaProducerAdapter(producer, topic, filePath);
        try {
            log.info("read {} and produce messages to kafka topic {}", filePath, topic);
            try (final FileReader fileReader = new FileReader(filePath)) {
                final var reader = new BufferedReader(fileReader);
                String message = "";
                while ((message = reader.readLine()) != null) {
                    if (filter(message)) {
                        Thread.sleep(waitMillis);
                        Future<RecordMetadata> metadata = producerApp.produce(message);
                        producerApp.printMetadata(metadata, filePath);
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
