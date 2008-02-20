package com.sd_editions.collatex.Collate;

import com.sd_editions.collatex.Block.Word;

public class NonAlignment extends Cell {
	private final Word base;
	private final Word witness;

	public NonAlignment(Word base, Word witness) {
		this.base = base;
		this.witness = witness;
	}
	
	@Override
	public String toString() {
		return "non-alignment: "+base.getContent()+", "+witness.getContent();
	}

	@Override
	public String toHTML() {
		return witness.getContent();
	}
	

}
