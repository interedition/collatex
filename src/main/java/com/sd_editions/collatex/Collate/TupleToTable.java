package com.sd_editions.collatex.Collate;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.Line;
import com.sd_editions.collatex.Block.Word;

public class TupleToTable {

	private Table table;
	private final Line base;
	private final Line witness;
	private final Tuple[] tuples;
	private int variant = 1;
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
			System.out.println("i: " + i + "; baseIndex: " + baseIndex + "; witnessIndex: " + witnessIndex);
			Tuple tuple = tuples[i];
			int difBaseIndex = tuple.baseIndex - baseIndex;
			int difWitnessIndex = tuple.witnessIndex - witnessIndex;
			if (difBaseIndex == 1 && difWitnessIndex == 1) {
				baseIndex = tuple.baseIndex;
				witnessIndex = tuple.witnessIndex;
				column = baseIndex * 2 - 2;
				addIdenticalToTable(base.get(baseIndex), witness.get(witnessIndex));
			} else if (difBaseIndex > 1 && difWitnessIndex > 1) {
				baseIndex = baseIndex+1;
				witnessIndex = witnessIndex+1;
				column = baseIndex * 2 - 2;
				addReplacementToTable(base.get(baseIndex), witness.get(witnessIndex));
				baseIndex = tuple.baseIndex;
				witnessIndex = tuple.witnessIndex;
				column = baseIndex * 2 - 2;
				addIdenticalToTable(base.get(tuple.baseIndex), witness.get(tuple.witnessIndex));
			} else if (difBaseIndex > 1 && difWitnessIndex == 1) {
				baseIndex = baseIndex+1;
				column = baseIndex * 2 - 2;
				addOmissionToTable(base.get(baseIndex));
				baseIndex = tuple.baseIndex;
				witnessIndex = tuple.witnessIndex;
				column = baseIndex * 2 - 2;
				addIdenticalToTable(base.get(tuple.baseIndex), witness.get(tuple.witnessIndex));
			} else if (difBaseIndex == 1 && difWitnessIndex > 1) {
				baseIndex = baseIndex+1;
				witnessIndex++;
				column = baseIndex * 2 - 2;
				addAdditionToTable(witness.get(witnessIndex));
				baseIndex = tuple.baseIndex;
				witnessIndex = tuple.witnessIndex;
				column = baseIndex * 2 - 2;
				addIdenticalToTable(base.get(tuple.baseIndex), witness.get(tuple.witnessIndex));
			}
		}

	}

	private void addOmissionToTable(Word baseWord) {
		Cell omission = new Omission(baseWord);
		addAlignmentInformationToResult(2, omission);
	}

	private void addAdditionToTable(Word witnessWord) {
		Cell addition = new Addition(witnessWord);
		addAlignmentInformationToResult(1, addition);
	}

	private void addReplacementToTable(Word baseWord, Word replacementWord) {
		Cell replacement = new Replacement(baseWord, replacementWord);
		addAlignmentInformationToResult(2, replacement);
	}

	private void addIdenticalToTable(Word baseWord, Word witnessWord) {
		Cell alignment;
		if (baseWord.alignmentFactor(witnessWord) == 0) {
			alignment = new AlignmentIdentical(baseWord, witnessWord);
		} else {
			alignment = new AlignmentVariant(baseWord, witnessWord);
		}
		addAlignmentInformationToResult(2, alignment);
	}

	private void addAlignmentInformationToResult(int offset, Cell alignment) {
		table.setCell(variant, column + offset, alignment);
	}

	public Table getTable() {
		return table;
	}

}
