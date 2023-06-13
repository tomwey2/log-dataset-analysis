package de.tomwey2.kafka.log.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaLogDataConsumer {

    public static void main(String[] args) {
        SpringApplication.run(KafkaLogDataConsumer.class, args);
    }

}
