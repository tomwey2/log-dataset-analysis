#!/bin/bash

KAFKA_HOME="/opt/kafka_2.13-3.4.0"
echo "kafka consumer console ..."
$KAFKA_HOME/bin/kafka-console-consumer.sh --topic $1 --from-beginning --bootstrap-server localhost:9092
