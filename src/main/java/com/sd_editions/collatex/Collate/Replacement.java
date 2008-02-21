package com.sd_editions.collatex.Collate;

import com.sd_editions.collatex.Block.Word;

public class Replacement extends Cell {

	private final Word baseWord;
	private final Word replacementWord;

	public Replacement(Word baseWord, Word replacementWord) {
		this.baseWord = baseWord;
		this.replacementWord = replacementWord;
	}

	@Override
	public String toHTML() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString() {
		return "replacement: "+baseWord.getContent()+" / "+replacementWord.getContent();
	}

}
