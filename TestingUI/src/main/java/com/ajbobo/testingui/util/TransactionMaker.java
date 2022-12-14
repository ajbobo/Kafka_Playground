package com.ajbobo.testingui.util;

import com.ajbobo.testingui.DecimalOuter.Decimal;
import com.ajbobo.testingui.TransactionOuter.Transaction;

import java.util.Random;

public class TransactionMaker {

	private static final Random rand = new Random();
	private static final String[] names = { "Andy", "Bill", "Carol", "Dave", "Edgar", "Fred", "George" };
	private static final String[] reasons = { "Food", "Movie", "Game", "Babysitting", "No Reason" };

	public static Transaction CreateTransaction() {

		return Transaction.newBuilder()
				.setTo(getArrayValue(names))
				.setFrom(getArrayValue(names))
				.setDescription(getArrayValue(reasons))
				.setAmount(Decimal.newBuilder()
						.setDollars(rand.nextInt(500))
						.setCents(rand.nextInt(100))
						.build())
				.build();
	}

	private static String getArrayValue(String[] arr) {
		return arr[rand.nextInt(arr.length)];
	}
}
