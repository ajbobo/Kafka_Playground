package com.ajbobo.testingui;

import com.ajbobo.testingui.kafka.MessageConsumer;
import com.ajbobo.testingui.kafka.MessageProducer;
import com.ajbobo.testingui.kafka.StreamsProcessor;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TestingUIApplication extends Application {
	private static final Logger logger = LoggerFactory.getLogger(TestingUIApplication.class);

	private MessageProducer messageProducer;
	private StreamsProcessor streamsProcessor;
	private MessageConsumer personConsumer;
	private MessageConsumer transactionConsumer;

	@Override
	public void start(Stage stage) throws IOException {
		logger.info("Starting TestingUI");

		messageProducer = new MessageProducer();
		streamsProcessor = new StreamsProcessor();
		personConsumer = new MessageConsumer("id.topic");
		transactionConsumer = new MessageConsumer("transaction.topic");
		streamsProcessor.start();

		FXMLLoader fxmlLoader = new FXMLLoader(TestingUIApplication.class.getResource("TestingUI.fxml"));
		Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
		TestingUIController controller = fxmlLoader.getController();
		controller.attachMessageProducer(messageProducer);
		controller.attachStreamsProcessor(streamsProcessor);
		controller.attachPersonConsumer(personConsumer);
		controller.attachTransactionConsumer(transactionConsumer);

		stage.setTitle("Kafka Streams Testing");
		stage.setScene(scene);
		stage.show();
	}

	@Override
	public void stop() {
		messageProducer.stop();
		streamsProcessor.stop();
		personConsumer.stop();
		transactionConsumer.stop();
	}

	public static void main(String[] args) {
		launch();
	}
}