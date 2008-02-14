package com.sd_editions.collatex.Collate;

import com.sd_editions.collatex.Block.Word;

public class AlignmentIdentical extends Cell {
	private final Word base;
	private final Word witness;

	public AlignmentIdentical(Word base, Word witness) {
		this.base = base;
		this.witness = witness;
	}

	@Override
	public String toString() {
		return "identical: "+base.getContent();
	}
	

}
