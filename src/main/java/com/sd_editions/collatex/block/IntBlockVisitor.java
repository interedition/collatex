package com.sd_editions.collatex.Block;


public interface IntBlockVisitor {

	public abstract void visitBlockStructure(BlockStructure blockStructure);

	public abstract void visitLine(Line line);

	public abstract void visitWord(Word word);

}