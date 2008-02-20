package com.sd_editions.collatex.Web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;
import com.sd_editions.collatex.Collate.Cell;
import com.sd_editions.collatex.Collate.Table;
import com.sd_editions.collatex.Collate.TextAlignmentVisitor;
import com.sd_editions.collatex.InputPlugin.StringInputPlugin;


public class Homepage extends WebPage {
	
	public Homepage() {
		ModelForView model = new ModelForView("a black cat", "a white cat");

		add(new Label("base", new PropertyModel(model, "base")));
		add(new Label("witness", new PropertyModel(model, "witness")));
		
		Label label = new Label("alignment", new PropertyModel(model, "html"));
		label.setEscapeModelStrings(false);
		add(label);
		add(new AlignmentForm("alignmentform", model));
	}
	
	class ModelForView  implements Serializable {
		private String base;
		private String witness;
		private String html;
		
		public ModelForView(String base, String witness){
			this.base = base;
			this.witness = witness;
			fillAlignmentTable();
		}

		private void fillAlignmentTable() {
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
			this.html = showAlignmentTable(alignment);
		}

		public void setBase(String base) {
			this.base = base;
		}

		public String getBase() {
			return base;
		}

		public void setWitness(String witness) {
			this.witness = witness;
		}

		public String getWitness() {
			return witness;
		}

		public void setHtml(String html) {
			this.html = html;
		}

		public String getHtml() {
			return html;
		}
	}
	
	class AlignmentForm extends Form {

		private final ModelForView modelForView;

		public AlignmentForm(String id, ModelForView modelForView) {
			super(id);
			this.modelForView = modelForView;
			add(new TextField("base", new PropertyModel(modelForView, "base")));
			add(new TextField("witness", new PropertyModel(modelForView, "witness")));
		}
		
		@Override
		protected void onSubmit() {
			System.out.println(modelForView.base);
			System.out.println(modelForView.witness);
			// TODO: this can be moved to model!
			modelForView.fillAlignmentTable(); 
		}
		
	}
	
	
	// TODO: move to Table class!
	private String showAlignmentTable(Table table) {
		String alignmentTableHTML = "<table border=\"1\">";
		alignmentTableHTML += "<tr>";
		for (int i = 1; i < 10; i++) {
			alignmentTableHTML += "<td>";
			alignmentTableHTML += ""+i;
			alignmentTableHTML += "</td>";
		}
		alignmentTableHTML += "</tr>";
		alignmentTableHTML = showRow(table, alignmentTableHTML, 0);
		alignmentTableHTML = showRow(table, alignmentTableHTML, 1);
		alignmentTableHTML += "</table>";
		return alignmentTableHTML;
	}

	// TODO: move to Table class! TODO: remove alignmentTableHTML parameter!
	private String showRow(Table table, String alignmentTableHTML, int row) {
		alignmentTableHTML += "<tr>";
		for (int i = 1; i < 10; i++) {
			Cell cell = table.get(row, i);
			alignmentTableHTML += "<td>";
			alignmentTableHTML += cell.toHTML();
			alignmentTableHTML += "</td>";
		}
		alignmentTableHTML += "</tr>";
		return alignmentTableHTML;
	}

}
