package com.ajbobo.testingui.util;

import com.ajbobo.testingui.PersonOuter.Person;
import com.ajbobo.testingui.TransactionOuter.Transaction;

public class Util {
	public static String PersonToString(Person person) {
		return String.format("%s %s %s - %s, %s",
				person.getFirstName(),
				person.getMiddleName(),
				person.getLastName(),
				person.getHome().getCity(),
				person.getHome().getState());
	}

	public static String TransactionToString(Transaction transaction) {
		return String.format("%s -> %s : $%d.%02d for %s",
				(transaction.getFrom().length() > 0 ? transaction.getFrom() : "?"),
				(transaction.getTo().length() > 0 ? transaction.getTo() : "??"),
				transaction.getAmount().getDollars(),
				transaction.getAmount().getCents(),
				(transaction.getDescription().length() > 0 ? transaction.getDescription() : "???"));
	}
}
