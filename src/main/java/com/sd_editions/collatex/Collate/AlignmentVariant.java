package com.sd_editions.collatex.Collate;

import com.sd_editions.collatex.Block.Word;

public class AlignmentVariant extends Cell {
	private final Word base;
	private final Word witness;

	public AlignmentVariant(Word base, Word witness) {
		this.base = base;
		this.witness = witness;
	}

	@Override
	public String toString() {
		return "variant-align: "+base.getContent()+ " / "+witness.getContent();
	}
	
}
