package com.sd_editions.collatex.Web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;
import com.sd_editions.collatex.Collate.LCS;
import com.sd_editions.collatex.Collate.Table;
import com.sd_editions.collatex.Collate.Tuple;
import com.sd_editions.collatex.Collate.TupleToTable;
import com.sd_editions.collatex.Collate.WordAlignmentVisitor;
import com.sd_editions.collatex.InputPlugin.StringInputPlugin;

@SuppressWarnings("serial")
public class Homepage extends WebPage {

  public Homepage() {
    ModelForView model = new ModelForView("the drought of march hath perced to the root", new String[] { "the march of the drought hath perced to the root",
        "the march of drought hath perced to the root" });
    //ModelForView model = new ModelForView("the big bug had a big head", new String[] { "the bug had a small head", "the bug had a small head" });
    //ModelForView model = new ModelForView("the bug big had a big head", new String[] { "the bug had a small head", "the bug had a small head" });
    //ModelForView model = new ModelForView("the black cat sat on the mat", new String[] { "the cat sat on the black mat", "the cat sat on the black mat" });
    //ModelForView model = new ModelForView("a cat or dog", new String[] { "a cat and dog and", "a cat and dog and" });
    //ModelForView model = new ModelForView("Auch hier hab ich wieder ein Pl�tzchen", new String[] { "Ich hab auch hier wieder ein Pl�zchen", "Ich hab auch hier wieder ein Pl�zchen" });
    //ModelForView model = new ModelForView("the black cat on the table", new String[] { "the black saw the black cat on the table", "the black saw the black cat on the table" });
    //ModelForView model = new ModelForView("the black cat and the black mat", new String[] { "the black dog and the black mat", "the black dog and the black mat" });

    add(new Label("base", new PropertyModel(model, "base")));
    add(new Label("witness1", new PropertyModel(model, "witness1")));
    add(new Label("witness2", new PropertyModel(model, "witness2")));

    Label label = new Label("alignment", new PropertyModel(model, "html"));
    label.setEscapeModelStrings(false);
    Label label2 = new Label("alignment2", new PropertyModel(model, "html2"));
    label2.setEscapeModelStrings(false);

    add(label);
    add(label2);
    add(new AlignmentForm("alignmentform", model));
  }

  @SuppressWarnings("serial")
  class ModelForView implements Serializable {
    private String base;
    private String[] witnesses;
    private String html;
    private String html2;

    public ModelForView(String newBase, String[] newWitnesses) {
      this.base = newBase;
      this.witnesses = newWitnesses;
      fillAlignmentTable_LCS();
      fillAlignmentTable();
    }

    void fillAlignmentTable_LCS() {
      BlockStructure baseStructure = string2BlockStructure(base.toLowerCase());
      List<BlockStructure> witnessList = new ArrayList<BlockStructure>();
      List<Tuple[]> resultList = new ArrayList<Tuple[]>();
      int i = 0;
      for (String witness : witnesses) {
        BlockStructure witnessStructure = string2BlockStructure(witness.toLowerCase());
        witnessList.add(witnessStructure);
        LCS tupSeq = new LCS(baseStructure, witnessList, i);
        resultList.add(tupSeq.getLCS());
        i++;
      }
      Tuple[][] results = resultList.toArray(new Tuple[][] {});
      Table alignment = new TupleToTable(baseStructure, witnessList, results).getTable();
      this.html = alignment.toHTML();
    }

    void fillAlignmentTable() {
      BlockStructure baseStructure = string2BlockStructure(base);
      List<BlockStructure> witnessList = new ArrayList<BlockStructure>();
      List<Tuple[]> resultList = new ArrayList<Tuple[]>();
      for (String witness : witnesses) {
        BlockStructure witnessStructure = string2BlockStructure(witness);
        WordAlignmentVisitor visitor = new WordAlignmentVisitor(witnessStructure);
        baseStructure.accept(visitor);
        witnessList.add(witnessStructure);
        resultList.add(visitor.getResult());
      }

      Tuple[][] results = resultList.toArray(new Tuple[][] {});
      Table alignment = new TupleToTable(baseStructure, witnessList, results).getTable();
      this.html2 = alignment.toHTML();
    }

    BlockStructure string2BlockStructure(String string) {
      BlockStructure result = null;
      try {
        result = new StringInputPlugin(string).readFile();
        // TODO: work away those exceptions.. they are not relevant for Strings
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (BlockStructureCascadeException e) {
        throw new RuntimeException(e);
      }
      return result;
    }

    public void setBase(String newBase) {
      this.base = newBase;
    }

    public String getBase() {
      return base;
    }

    public void setWitness1(String witness) {
      this.witnesses[0] = witness;
    }

    public String getWitness1() {
      return witnesses[0];
    }

    public void setWitness2(String witness) {
      this.witnesses[1] = witness;
    }

    public String getWitness2() {
      return witnesses[1];
    }

    public void setHtml(String newHtml) {
      this.html = newHtml;
    }

    public String getHtml() {
      return html;
    }

    public String getHtml2() {
      return html2;
    }

  }

  @SuppressWarnings("serial")
  class AlignmentForm extends Form {
    private final ModelForView modelForView;

    public AlignmentForm(String id, ModelForView myModelForView) {
      super(id);
      this.modelForView = myModelForView;
      add(new TextField("base", new PropertyModel(modelForView, "base")));
      add(new TextField("witness1", new PropertyModel(modelForView, "witness1")));
      add(new TextField("witness2", new PropertyModel(modelForView, "witness2")));
    }

    @Override
    protected void onSubmit() {
      // NOTE: this can be moved to model!
      modelForView.fillAlignmentTable();
      modelForView.fillAlignmentTable_LCS();
    }

  }

}
