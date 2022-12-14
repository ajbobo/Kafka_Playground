package com.ajbobo.testingui.kafka;

public abstract class ExternalOutput {
	public interface OutputFunction {
		void output(String text);
	}

	protected OutputFunction outputFunc;

	public void setOutputFunction(OutputFunction func) {outputFunc = func;}
}
