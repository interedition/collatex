package com.sd_editions.collatex.Collate;

import java.util.ArrayList;
import java.util.List;

import com.sd_editions.collatex.Block.Line;
import com.sd_editions.collatex.Block.Word;

public class TableView {
	private List<List<String>> table; 
	public TableView(Line baseLine1, Line variantLine1) {
		this.table = new ArrayList<List<String>>();
		List<String> row1 = new ArrayList<String>();
		List<String> row2 = new ArrayList<String>();
		this.table.add(row1);
		this.table.add(row2);
		putLineInRow(baseLine1, row1);
		putLineInRow(variantLine1, row2);
	}

	private void putLineInRow(Line baseLine1, List<String> row1) {
		Word word = (Word) baseLine1.getFirstChild();
		addWordToARow(row1, word);
		while (word.hasNextSibling()) {
			word = (Word) word.getNextSibling();
			addWordToARow(row1, word);
		}
	}

	private void addWordToARow(List<String> row1, Word word) {
		row1.add(word.getContent());
	}

	public String getWord(int i, int j) {
		return table.get(i).get(j);
	}

}
