#!/bin/bash

KAFKA_HOME="/home/tom/Code/sys/kafka/kafka_2.13-3.4.0"
echo "create kafka topics ..."
$KAFKA_HOME/bin/kafka-topics.sh --create --topic log-raw-data --bootstrap-server localhost:9092
$KAFKA_HOME/bin/kafka-topics.sh --create --topic purchases --bootstrap-server localhost:9092
$KAFKA_HOME/bin/kafka-topics.sh --list --bootstrap-server localhost:9092
