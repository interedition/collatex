package com.sd_editions.collatex.Collate;

import com.sd_editions.collatex.Block.Block;
import com.sd_editions.collatex.Block.IntBlockVisitor;
import com.sd_editions.collatex.Block.Word;

public class Omission extends Block {
	private final Word base;

	public Omission(Word base) {
		this.base = base;
	}

	@Override
	public String toString() {
		return "omission: " + base.getContent();
	}

	@Override
	public void accept(IntBlockVisitor visitor) {
		// TODO Auto-generated method stub

	}

}
