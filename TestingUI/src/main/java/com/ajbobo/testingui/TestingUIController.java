package com.ajbobo.testingui;

import com.ajbobo.testingui.kafka.MessageConsumer;
import com.ajbobo.testingui.kafka.MessageProducer;
import com.ajbobo.testingui.kafka.StreamsProcessor;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TestingUIController {
	private static final Logger logger = LoggerFactory.getLogger(TestingUIController.class);

	private static final int NUM_THREADS = 4;
	private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(NUM_THREADS,
			r -> { // This sets all new threads in this pool to be daemon threads
				Thread t = Executors.defaultThreadFactory().newThread(r);
				t.setDaemon(true);
				return t;
			});

	private MessageProducer messageProducer;
	private ScheduledFuture<?> producerFuture;
	private MessageConsumer personMessageConsumer;
	private ScheduledFuture<?> personConsumerFuture;
	private MessageConsumer transactionMessageConsumer;
	private ScheduledFuture<?> transactionConsumerFuture;

	@FXML
	private TextArea txtProducerOutput;
	@FXML
	private TextArea txtStreamOutput;
	@FXML
	private TextArea txtPersonOutput;
	@FXML
	private TextArea txtTransactionOutput;
	@FXML
	private Button btnProducerStart;
	@FXML
	private Button btnProducerStop;
	@FXML
	private Button btnPersonStart;
	@FXML
	private Button btnPersonStop;
	@FXML
	private Button btnTransactionStart;
	@FXML
	private Button btnTransactionStop;

	private void writeToTextArea(String message, TextArea txtArea) {
		for (String str : message.split("\r\n")) {
			txtArea.appendText(str + "\r\n");
		}
	}

	public void attachMessageProducer(MessageProducer producer) {
		messageProducer = producer;
		producer.setOutputFunction(str -> writeToTextArea(str, txtProducerOutput));
	}
	public void attachStreamsProcessor(StreamsProcessor streams) {
		streams.setOutputFunction(str -> writeToTextArea(str, txtStreamOutput));
	}
	public void attachPersonConsumer(MessageConsumer consumer) {
		personMessageConsumer = consumer;
		consumer.setOutputFunction(str -> writeToTextArea(str, txtPersonOutput));
	}
	public void attachTransactionConsumer(MessageConsumer consumer) {
		transactionMessageConsumer = consumer;
		consumer.setOutputFunction(str -> writeToTextArea(str, txtTransactionOutput));
	}

	@FXML
	protected void startProducer() {
		btnProducerStart.setDisable(true);
		btnProducerStop.setDisable(false);
		txtProducerOutput.textProperty().setValue("");

		producerFuture = executor.scheduleAtFixedRate(messageProducer, 1, 1, TimeUnit.SECONDS);
	}

	@FXML
	protected void stopProducer() {
		if (null != messageProducer) {
			btnProducerStart.setDisable(false);
			btnProducerStop.setDisable(true);
			messageProducer.stop();
			producerFuture.cancel(false);
		}
	}

	@FXML
	protected void startPersonConsumer() {
		btnPersonStart.setDisable(true);
		btnPersonStop.setDisable(false);
		txtPersonOutput.textProperty().setValue("");

		personConsumerFuture = executor.scheduleAtFixedRate(personMessageConsumer, 10, 10, TimeUnit.MILLISECONDS);
	}

	@FXML
	protected void stopPersonConsumer() {
		if (null != personMessageConsumer) {
			btnPersonStart.setDisable(false);
			btnPersonStop.setDisable(true);
			personMessageConsumer.stop();
			personConsumerFuture.cancel(false);
		}
	}

	@FXML
	protected void startTransactionConsumer() {
		btnTransactionStart.setDisable(true);
		btnTransactionStop.setDisable(false);
		txtTransactionOutput.textProperty().setValue("");

		transactionConsumerFuture = executor.scheduleAtFixedRate(transactionMessageConsumer, 10, 10, TimeUnit.MILLISECONDS);
	}

	@FXML
	protected void stopTransactionConsumer() {
		if (null != transactionMessageConsumer) {
			btnTransactionStart.setDisable(false);
			btnTransactionStop.setDisable(true);
			transactionMessageConsumer.stop();
			transactionConsumerFuture.cancel(false);
		}
	}
}