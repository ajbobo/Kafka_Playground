package com.ajbobo.ktabletest;

import KTableTest.EnabledObjectOuterClass.EnabledObject;
import KTableTest.TestObjectOuterClass.TestObject;
import com.ajbobo.ktabletest.util.AppProperties;
import com.ajbobo.ktabletest.util.SerdeFactory;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class StreamsRunner {
  Logger logger = LoggerFactory.getLogger(StreamsRunner.class);
  private final KafkaStreams streams;

  public StreamsRunner(Properties props, String inputTopic, String outputTopic) {
    logger.info("Configuring StreamsRunner");

    StreamsBuilder builder = new StreamsBuilder();

    SerdeFactory factory = new SerdeFactory(AppProperties.getProperty("schema.registry.url"));
    Serde<TestObject> testObjectSerde = factory.serde(TestObject.class);
    Serde<EnabledObject> enabledObjectSerde = factory.serde(EnabledObject.class);

    // This will materialize the entire flattened table
    KTable<String, TestObject> table = builder.table(inputTopic,
        Materialized.<String, TestObject, KeyValueStore<Bytes, byte[]>>as("Ktable-store")
            .withKeySerde(Serdes.String())
            .withValueSerde(testObjectSerde));

    // This will run every 30 seconds and get the flattened data from what came in during that window
    // It then pipes it to a new Topic
    table.toStream()
        .peek((k, v) -> logger.info("Table record: ({}, {}|{}|{}|{})", k, v.getName(), v.getVal1(), v.getVal2(), v.getEnabled()))
        .mapValues((k, v) -> EnabledObject.newBuilder().setName(k).setEnabled(v.getEnabled()).build())
        .peek((k, v) -> logger.info("Enabled record: ({}, {}|{})", k, v.getName(), v.getEnabled()))
        .toTable(Materialized.<String, EnabledObject, KeyValueStore<Bytes, byte[]>>as("enabled-store") // This is only updated every 30 seconds
            .withKeySerde(Serdes.String())
            .withValueSerde(enabledObjectSerde))
        .toStream()
        .to(outputTopic, Produced.with(Serdes.String(), enabledObjectSerde));

    streams = new KafkaStreams(builder.build(), props);

    logger.info("StreamsRunner configuration complete");
  }

  public void getMaterializedData() {
    logger.info("--- KTable-store ---");
    StoreQueryParameters<ReadOnlyKeyValueStore<String, TestObject>> params = StoreQueryParameters.fromNameAndType("Ktable-store", QueryableStoreTypes.keyValueStore());
    ReadOnlyKeyValueStore<String, TestObject> store = streams.store(params);

    KeyValueIterator<String, TestObject> range = store.all();
    while (range.hasNext()) {
      KeyValue<String, TestObject> item = range.next();
      logger.info("{} => {}|{}|{}|{}", item.key, item.value.getName(), item.value.getVal1(), item.value.getVal2(), item.value.getEnabled());
    }
    range.close();

    logger.info("--- Enabled-store ---");
    StoreQueryParameters<ReadOnlyKeyValueStore<String, EnabledObject>> params2 = StoreQueryParameters.fromNameAndType("enabled-store", QueryableStoreTypes.keyValueStore());
    ReadOnlyKeyValueStore<String, EnabledObject> store2 = streams.store(params2);

    KeyValueIterator<String, EnabledObject> range2 = store2.all();
    while (range2.hasNext()) {
      KeyValue<String, EnabledObject> item = range2.next();
      logger.info("{} => {}|{}", item.key, item.value.getName(), item.value.getEnabled());
    }
    range2.close();
  }

  public void start() {
    logger.info("Starting Streams processing");
    streams.start();
    logger.info("Running");
  }

  public void stop() {
    logger.info("Stopping Streams processing");
    streams.close();
  }
}
