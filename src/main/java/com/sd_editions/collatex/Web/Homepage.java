package com.sd_editions.collatex.Web;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;
import com.sd_editions.collatex.Collate.Cell;
import com.sd_editions.collatex.Collate.Table;
import com.sd_editions.collatex.Collate.TextAlignmentVisitor;
import com.sd_editions.collatex.InputPlugin.StringInputPlugin;


public class Homepage extends WebPage {
	
	public Homepage() {
		String base = "a black cat";
		String witness = "a white cat";

		BlockStructure baseStructure;
		BlockStructure witnessStructure;
		try {
			baseStructure = new StringInputPlugin(base).readFile();
			witnessStructure = new StringInputPlugin(witness).readFile();
		} catch (FileNotFoundException e) { // TODO: work away those exceptions.. they are not relevant for Strings
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (BlockStructureCascadeException e) {
			throw new RuntimeException(e);
		}
		
		TextAlignmentVisitor visitor = new TextAlignmentVisitor(witnessStructure);
		baseStructure.accept(visitor);
		BlockStructure result = visitor.getResult();
		Table alignment = (Table) result.getRootBlock();
		
		add(new Label("base", base));
		add(new Label("witness", witness));
		
		Label label = new Label("alignment", showAlignmentTable(alignment));
		label.setEscapeModelStrings(false);
		add(label);
	}
	
	private String showAlignmentTable(Table table) {
		String alignmentTableHTML = "<table border=\"1\">";
		alignmentTableHTML += "<tr>";
		for (int i = 1; i < 10; i++) {
			alignmentTableHTML += "<td>";
			alignmentTableHTML += ""+i;
			alignmentTableHTML += "</td>";
		}
		alignmentTableHTML += "</tr>";
		alignmentTableHTML += "<tr>";
		for (int i = 1; i < 10; i++) {
			Cell cell = table.get(1, i);
			alignmentTableHTML += "<td>";
			alignmentTableHTML += cell.toHTML();
			alignmentTableHTML += "</td>";
		}
		alignmentTableHTML += "</tr>";
		alignmentTableHTML += "</table>";
		return alignmentTableHTML;
	}

}
