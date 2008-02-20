package com.sd_editions.collatex.Collate;

import com.sd_editions.collatex.Block.Word;

public class Addition extends Cell {

	private final Word witnessWord;

	public Addition(Word witnessWord) {
		this.witnessWord = witnessWord;
	}

	@Override
	public String toString() {
		return "addition: "+witnessWord.getContent();
	}
	@Override
	public String toHTML() {
		return witnessWord.getContent();
	}
	
}
