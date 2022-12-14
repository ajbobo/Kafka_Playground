package com.ajbobo.testingui.kafka;

import com.ajbobo.testingui.TransactionOuter.Transaction;
import com.ajbobo.testingui.util.AppProperties;
import com.google.protobuf.Message;
import io.confluent.kafka.streams.serdes.protobuf.KafkaProtobufSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY;

public class StreamsProcessor extends ExternalOutput {
	private static final Logger logger = LoggerFactory.getLogger(StreamsProcessor.class);

	private final KafkaStreams streams;

	private KafkaProtobufSerde<Message> getProtoSerde() {
		final KafkaProtobufSerde<Message> protobufSerde = new KafkaProtobufSerde<>();
		Map<String, String> serdeConfig = new HashMap<>();
		serdeConfig.put(SCHEMA_REGISTRY_URL_CONFIG, AppProperties.getProperty(SCHEMA_REGISTRY_URL_CONFIG));
		serdeConfig.put(VALUE_SUBJECT_NAME_STRATEGY, AppProperties.getProperty(VALUE_SUBJECT_NAME_STRATEGY));
		serdeConfig.put("derive.type", AppProperties.getProperty("derive.type"));
		protobufSerde.configure(serdeConfig, false);
		return protobufSerde;
	}

	public StreamsProcessor() {
		String inTopic = AppProperties.getProperty("input.topic");
		String idTopic = AppProperties.getProperty("id.topic");
		String transactionTopic = AppProperties.getProperty("transaction.topic");

		KafkaProtobufSerde<Message> protobufSerde = getProtoSerde();
		StreamsBuilder builder = new StreamsBuilder();
		KStream<String, Message> inputStream = builder.stream(inTopic, Consumed.with(Serdes.String(), protobufSerde));
		Map<String, KStream<String, Message>> branches = inputStream
				.peek((key, value) -> {
					logger.info("StreamPeek {}", key); if (null != outputFunc) outputFunc.output(key);
				})
				.split(Named.as("Branch-"))
				.branch((key, value) -> key.startsWith("ID"), Branched.as(idTopic))
				.branch((key, value) -> key.startsWith("Tr"), Branched.as(transactionTopic))
				.defaultBranch(Branched.as("Unknown"));

		branches.get("Branch-" + idTopic)
				.peek((key, value) -> {
					logger.info("ID -- {} --", value); if (null != outputFunc) outputFunc.output("-> ID");
				})
				.to(idTopic, Produced.with(Serdes.String(), protobufSerde));

		branches.get("Branch-" + transactionTopic)
				.peek((key, value) -> logger.info("TR Dollars: {}", ((Transaction) value).getAmount().getDollars()))
				.filter((key, value) -> ((Transaction) value).getAmount().getDollars() > 200)
				.peek((key, value) -> {
					logger.info("TR -- {} --", value); if (null != outputFunc) outputFunc.output("-> Transaction");
				})
				.to(transactionTopic, Produced.with(Serdes.String(), protobufSerde));

		branches.get("Branch-Unknown")
				.peek((key, value) -> {
					logger.info("?? -- {} --", value); if (null != outputFunc) outputFunc.output("-> Unknown");
				})
				.to(idTopic, Produced.with(Serdes.String(), protobufSerde));

		streams = new KafkaStreams(builder.build(), AppProperties.getAll());
	}

	public void start() {
		logger.info("Starting Streams Processor");
		streams.start();
	}

	public void stop() {
		logger.info("Stopping Streams Processor");
		streams.close();
	}
}
