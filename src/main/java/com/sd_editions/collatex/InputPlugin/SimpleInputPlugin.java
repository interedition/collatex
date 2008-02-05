package com.sd_editions.collatex.InputPlugin;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

/**
 * An example plugin this simple reading in a text file and breaks down each
 * word (a word being defined as anything between a non-spacing character).
 * 
 */

public class SimpleInputPlugin extends AbstractPlainTextInputPlugin {

	private String filename;

	public SimpleInputPlugin() {
		this.filename = null;
	}

	public SimpleInputPlugin(String filename) {
		this.filename = filename;
	}

	@Override
	protected Reader getReader() throws FileNotFoundException {
		return new FileReader(this.filename);
	}

}
