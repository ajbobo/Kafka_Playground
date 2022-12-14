package examples;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Util {

	public static Properties loadConfig(final String configFile) throws IOException {
		final Properties cfg = new Properties();
		try (InputStream inputStream = Util.class.getClassLoader().getResourceAsStream(configFile)) {
			cfg.load(inputStream);
		}
		return cfg;
	}
}
