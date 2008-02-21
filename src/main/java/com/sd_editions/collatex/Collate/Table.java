package com.sd_editions.collatex.Collate;

import com.sd_editions.collatex.Block.Block;
import com.sd_editions.collatex.Block.IntBlockVisitor;

public class Table extends Block {

	private Cell[][] cells;
	private final int baselinesize;

	public Table(int baselinesize) {
		this.baselinesize = baselinesize;
		this.cells = new Cell[10][size()+1];
	}

	private int size() {
		return baselinesize*2+1;
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

	public String toHTML() {
		String alignmentTableHTML = "<table border=\"1\">";
		alignmentTableHTML += "<tr><td>Numbers</td>";
		for (int i = 1; i <= size(); i++) {
			alignmentTableHTML += "<td>";
			alignmentTableHTML += "" + i;
			alignmentTableHTML += "</td>";
		}
		alignmentTableHTML += "</tr>";
		alignmentTableHTML += showRow(0, "Base");
		alignmentTableHTML += showRow(1, "Witness");
		alignmentTableHTML += "</table>";
		return alignmentTableHTML;
	}

	private String showRow(int row, String label) {
		String alignmentTableHTML = "<tr><td>"+label+"</td>";
		for (int i = 1; i <= size(); i++) {
			Cell cell = get(row, i);
			alignmentTableHTML += "<td>";
			alignmentTableHTML += cell.toHTML();
			alignmentTableHTML += "</td>";
		}
		alignmentTableHTML += "</tr>";
		return alignmentTableHTML;
	}

}
