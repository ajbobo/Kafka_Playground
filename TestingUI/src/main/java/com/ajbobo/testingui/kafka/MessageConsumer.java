package com.ajbobo.testingui.kafka;

import com.ajbobo.testingui.PersonOuter.Person;
import com.ajbobo.testingui.TransactionOuter.Transaction;
import com.ajbobo.testingui.util.AppProperties;
import com.google.protobuf.Message;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;

public class MessageConsumer extends ExternalOutput implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

	private boolean needClose = false;
	private Consumer<String, Message> consumer;
	private final String topic;

	public MessageConsumer(String topicProperty) {
		this.topic = AppProperties.getProperty(topicProperty);
	}

	@Override
	public void run() {
		if (this.needClose) {
			shutDown();
			return;
		}

		if (consumer == null) {
			consumer = new KafkaConsumer<>(AppProperties.getAll());
			consumer.subscribe(Collections.singletonList(topic));
		}

		ConsumerRecords<String, Message> records;
		records = consumer.poll(Duration.ofMillis(100));
		for (ConsumerRecord<String, Message> record : records) {
			outputFunc.output("Got a " + record.value().getClass().getTypeName());

			String key = record.key();
			logger.info("Read {} - {}", key, record.value());
			if (key.startsWith("ID")) {
				Person value = (Person) record.value();
				outputFunc.output(String.format("%s => %s", key, value));
			}
			else if (key.startsWith("Tr")) {
				Transaction value = (Transaction) record.value();
				outputFunc.output(String.format("%s => %s", key, value));
			}
			else {
				outputFunc.output(String.format("Unexpected\r\n%s -- %s", key, record.value()));
			}
		}

	}

	public void stop() {
		logger.info("Stopping MessageConsumer: {}", topic);
		this.needClose = true;
	}

	private void shutDown() {
		consumer.close();
		consumer = null;
		needClose = false;
	}
}
