package com.ajbobo.testingui.util;

import com.ajbobo.testingui.LocationOuter.Location;
import com.ajbobo.testingui.PersonOuter.Person;

import java.util.Random;

public class PersonMaker {

	private static final String[] firstNames = { "Andy", "Bill", "Carol", "Dave", "Edgar", "Fred", "George" };
	private static final String[] lastNames = { "Anderson", "Bailey", "Connor", "Duarte", "Emerson", "Franklin", "Gunn" };
	private static final String[] cities = { "Huntsville", "Oak Ridge", "Germantown", "Rockville", "Santa Fe", "Hatch" };
	private static final String[] states = { "AL", "AZ", "NM", "UT", "TX", "TN", "MD" };

	private static final Random rand = new Random();

	public static Person CreatePerson() {
		Person.Builder builder = Person.newBuilder()
				.setFirstName(getArrayValue(firstNames))
				.setLastName(getArrayValue(lastNames))
				.setHome(Location.newBuilder()
						.setCity(getArrayValue(cities))
						.setState(getArrayValue(states))
						.build());

		if (rand.nextInt(3) == 0)
			builder.setMiddleName(getArrayValue(firstNames));

		return builder.build();
	}

	private static String getArrayValue(String[] arr) {
		return arr[rand.nextInt(arr.length)];
	}
}
