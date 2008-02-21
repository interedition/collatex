package com.sd_editions.collatex.Collate;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.Line;
import com.sd_editions.collatex.Block.Word;

public class TupleToTable {

	private Table table;
	private final Line base;
	private final Line witness;
	private final Tuple[] tuples;
	private int variant=1;
	private int column;

	public TupleToTable(BlockStructure base, BlockStructure variant,
			Tuple[] tuples) {
		this.tuples = tuples;
		this.base = (Line) base.getRootBlock();
		this.witness = (Line) variant.getRootBlock();
		this.table = new Table(this.base.size());
		fillTable();
	}

	private void fillTable() {
		int baseIndex = 0;
		int witnessIndex = 0;
		for (int i = 0; i < tuples.length; i++) {
			Tuple tuple = tuples[i];
			int difBaseIndex = tuple.baseIndex - baseIndex;
			baseIndex =  tuple.baseIndex;
			int difWitnessIndex = tuple.witnessIndex - witnessIndex;
			witnessIndex = tuple.witnessIndex;
			if (difBaseIndex ==1 && difWitnessIndex == 1) {
				column = baseIndex*2-2;
				addIdenticalToTable(base.get(baseIndex), witness.get(witnessIndex));
			}
		}
		
	}

	private void addIdenticalToTable(Word baseWord, Word witnessWord) {
		Cell alignment;
		if (baseWord.alignmentFactor(witnessWord)==0) {
			alignment = new AlignmentIdentical(baseWord, witnessWord);			
		} else {
			alignment = new AlignmentVariant(baseWord, witnessWord);
		}
		addAlignmentInformationToResult(2, alignment);
	}


	private void addAlignmentInformationToResult(int offset, Cell alignment) {
		table.setCell(variant, column+offset, alignment);
	}
	

	public Table getTable() {
		return table;
	}

}
