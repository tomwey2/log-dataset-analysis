#!/bin/bash

KAFKA_HOME="/home/tom/Code/sys/kafka/kafka_2.13-3.4.0"
KTMPFILE="/tmp/kafka-logs"

if [ -f "$KTMPFILE" ]; then
    echo "delete kafka logs in $KTMPFILE"
    rm -rf $KTMPFILE
fi
echo "starting kafka ..."
$KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties
