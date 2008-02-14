package com.sd_editions.collatex.Collate;

import com.sd_editions.collatex.Block.Block;
import com.sd_editions.collatex.Block.IntBlockVisitor;

public class Table extends Block {

	private Cell[][] cells;

	public Table() {
		this.cells = new Cell[10][10]; // TODO: make dimensions flexible
	}
	
	@Override
	public void accept(IntBlockVisitor visitor) {
		// TODO Auto-generated method stub
		
	}
	
	public Cell get(int variant, int word) {
		Cell cell = cells[variant][word];
		if (cell == null)
			return Empty.getInstance();
		return cell;
	}

	public void setCell(int variant, int column, Cell alignment) {
		cells[variant][column] = alignment;
	}

}
