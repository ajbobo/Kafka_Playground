# This assumes that Kafka was created in a project called "kafka_local", and so a network called "kafka_local_default" was created
docker run -p 8080:8080 `
    --name 'kafka_ui' `
    -e 'KAFKA_CLUSTERS_0_NAME=local' `
    -e 'KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=broker:29092' `
    --network 'kafka_local_default' `
    -d provectuslabs/kafka-ui:latest
