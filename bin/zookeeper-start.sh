#!/bin/bash

KAFKA_HOME="/home/tom/Code/sys/kafka/kafka_2.13-3.4.0"
ZTMPFILE="/tmp/zookeeper"

if [ -f "$ZTMPFILE" ]; then
    echo "delete zookeeper logs in  $ZTMPFILE"
    rm -rf $ZTMPFILE
fi
echo "starting zookeeper ..."
$KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/config/zookeeper.properties


