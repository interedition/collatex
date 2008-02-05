package com.sd_editions.collatex.InputPlugin;

import java.io.Reader;
import java.io.StringReader;

public class StringInputPlugin extends AbstractPlainTextInputPlugin {

	private String text;

	public StringInputPlugin(String text) {
		this.text = text;
	}

	@Override
	protected Reader getReader() {
		return new StringReader(text);
	}

}
