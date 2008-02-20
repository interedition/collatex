package com.sd_editions.collatex.Collate;

import com.sd_editions.collatex.Block.Word;

public class BaseWord extends Cell {

	private final Word baseWord;

	public BaseWord(Word baseWord) {
		this.baseWord = baseWord;
	}

	@Override
	public String toHTML() {
		return baseWord.getContent();
	}

}
