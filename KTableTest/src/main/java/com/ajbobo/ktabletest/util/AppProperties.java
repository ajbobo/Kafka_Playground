package com.ajbobo.ktabletest.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class AppProperties {
	private static final Logger logger = LoggerFactory.getLogger(AppProperties.class);
	private static Properties props = null;

	private AppProperties()  {
		// Do nothing
	}

	private static void getProps(){
		try {
			logger.debug("Reading Properties for the first time");

			props = new Properties();
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			try (InputStream istream = loader.getResourceAsStream("streams.properties")) {
				props.load(istream);
			}
		}
		catch (Exception ex) {
			logger.error(ex.getMessage());
		}
	}

	public static String getProperty(String propName)
	{
		if (props == null)
			getProps();

		String val = props.getProperty(propName);
		logger.debug("Reading Property {} => {}", propName, val);

		return val;
	}

	public static Properties getAll(){
		if (props == null)
			getProps();

		return props;
	}
}
