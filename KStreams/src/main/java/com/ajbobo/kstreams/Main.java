package com.ajbobo.kstreams;


import com.ajbobo.kstreams.TestObjectOuterClass.TestObject;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Main {
  private static Logger logger = LoggerFactory.getLogger(Main.class);

  private static final String APPLICATION_ID = "KStreams-test";
  private static final String BOOTSTRAP_SERVER = "http://localhost:9092";
  private static final String SCHEMA_REGISTRY = "http://localhost:8081";
  private static final String TOPIC = "KTableTest";


  public static void main(String[] args) throws InterruptedException {
    logger.info("Starting");

    Properties props = getProps();

    KafkaProtobufSerde<TestObject> testObjectSerde = getTestObjectSerde();

    StreamsBuilder builder = new StreamsBuilder();
//    KStream<String, TestObject> stream = builder.stream(TOPIC, Consumed.with(Serdes.String(), testObjectSerde));
//
//    stream
//        .peek((k, v) -> logger.info("Before Filter {} => {}|{}|{}|{}", k, v.getName(), v.getVal1(), v.getVal2(), v.getEnabled()))
//        .filter((k, v) -> v.getEnabled())
//        .peek((k, v) -> logger.info("Filtered {} => {}|{}|{}|{}", k, v.getName(), v.getVal1(), v.getVal2(), v.getEnabled()))
//        .to("EnabledOnly", Produced.with(Serdes.String(), testObjectSerde));

    // This gets the most data if the application is reset to offset 0 before this is run (ResetKafkaApp.ps1)
    // Otherwise, it gets table data from wherever it currently is in the stream until its read timer runs out (30 seconds)
    KTable<String, TestObject> table = builder.table(TOPIC,
        Materialized.<String, TestObject, KeyValueStore<Bytes, byte[]>>as("ktable-test")
            .withKeySerde(Serdes.String())
            .withValueSerde(testObjectSerde));

    table.toStream()
        .peek((k, v) -> logger.info("Entry: {} => {}|{}|{}|{}", k, v.getName(), v.getVal1(), v.getVal2(), v.getEnabled()));


    KafkaStreams streams = new KafkaStreams(builder.build(), props);
    streams.start();

    Thread.sleep(30000);
    logger.info("Closing up");
    streams.close();
  }

  @NotNull
  private static Properties getProps() {
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
    props.put("schema.registry.url", SCHEMA_REGISTRY);
    props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT");
    props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
    props.put(CommonClientConfigs.CLIENT_DNS_LOOKUP_CONFIG, "use_all_dns_ips");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put("derive.type", "true");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer");
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer");
    props.put("value.subject.name.strategy", "io.confluent.kafka.serializers.subject.RecordNameStrategy");
    props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
    return props;
  }

  @NotNull
  private static KafkaProtobufSerde<TestObject> getTestObjectSerde() {
    KafkaProtobufSerde<TestObject> testObjectSerde = new KafkaProtobufSerde<>(TestObject.class);
    Map<String, String> serdeConfig = new HashMap<>();
    serdeConfig.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, KafkaProtobufSerde.class.getName());
    serdeConfig.put(KafkaProtobufDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY);
    serdeConfig.put(KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, TestObject.class.getName());
    testObjectSerde.configure(serdeConfig, false);
    return testObjectSerde;
  }
}