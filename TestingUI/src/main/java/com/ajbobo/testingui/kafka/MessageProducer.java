package com.ajbobo.testingui.kafka;

import com.ajbobo.testingui.PersonOuter.Person;
import com.ajbobo.testingui.TransactionOuter.Transaction;
import com.ajbobo.testingui.util.AppProperties;
import com.ajbobo.testingui.util.PersonMaker;
import com.ajbobo.testingui.util.TransactionMaker;
import com.ajbobo.testingui.util.Util;
import com.google.protobuf.Message;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class MessageProducer extends ExternalOutput implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(MessageProducer.class);

	private static final Random rand = new Random();
	private int counter;
	private boolean needsClose = false;
	private Producer<String, Message> producer;

	@Override
	public void run() {
		if (needsClose) {
			shutDown();
			return;
		}

		if (producer == null)
			producer = new KafkaProducer<>(AppProperties.getAll());

		final String topic = AppProperties.getProperty("input.topic");

		String key;
		Message value;
		if (rand.nextBoolean()) {
			key = String.format("ID_%05d", counter);
			value = PersonMaker.CreatePerson();
			logger.info("Person: {}", Util.PersonToString((Person) value));
		}
		else {
			key = String.format("Tr_%05d", counter);
			value = TransactionMaker.CreateTransaction();
			logger.info("Transaction: {}", Util.TransactionToString((Transaction) value));
		}

		final String outputStr = String.format("%s => %s", key, value);
		producer.send(
				new ProducerRecord<>(topic, key, value),
				(event, ex) -> {
					if (ex != null)
						logger.error(ex.getMessage());
					else if (null != outputFunc) outputFunc.output(outputStr);
				});

		counter++;
	}

	public void stop() {
		logger.info("Stopping MessageProducer");
		this.needsClose = true;
	}

	private void shutDown() {
		producer.close();
		producer = null;
		needsClose = false;
	}
}
