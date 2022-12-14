package examples;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;

public class ProducerExample {

	public static void main(final String[] args) throws IOException {
		if (args.length != 1) {
			System.out.println("Please provide the configuration file path as a command line argument");
			System.exit(1);
		}

		// Load producer configuration settings from a local file
		final Properties props = Util.loadConfig(args[0]);
		final String topic = "purchases";

		// Create a random message
		String[] users = { "eabara", "jsmith", "sgarcia", "jbernard", "htanaka", "awalther" };
		String[] items = { "book", "alarm clock", "t-shirts", "gift card", "batteries" };
		try (final Producer<String, String> producer = new KafkaProducer<>(props)) {
			final Random rnd = new Random();
			final int numMessages = 10;
			for (int i = 0; i < numMessages; i++) {
				String user = users[rnd.nextInt(users.length)];
				String item = items[rnd.nextInt(items.length)];

				producer.send(
						new ProducerRecord<>(topic, user, item),
						(event, ex) -> {
							if (ex != null)
								ex.printStackTrace();
							else
								System.out.printf("Produced event to topic %s: key = %-10s value = %s%n", topic, user, item);
						});
			}
			System.out.printf("%s events were produced to topic %s%n", numMessages, topic);
		}

	}
}