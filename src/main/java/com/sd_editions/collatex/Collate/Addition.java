package com.sd_editions.collatex.Collate;

import com.sd_editions.collatex.Block.Block;
import com.sd_editions.collatex.Block.IntBlockVisitor;
import com.sd_editions.collatex.Block.Word;

public class Addition extends Block {

	private final Word witnessWord;

	public Addition(Word witnessWord) {
		this.witnessWord = witnessWord;
	}

	@Override
	public String toString() {
		return "addition: "+witnessWord.getContent();
	}
	
	@Override
	public void accept(IntBlockVisitor visitor) {
		// TODO Auto-generated method stub

	}

}
