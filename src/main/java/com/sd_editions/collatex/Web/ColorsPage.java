package com.sd_editions.collatex.Web;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.PropertyModel;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.permutations.CollateCore;
import com.sd_editions.collatex.permutations.Witness;
import com.sd_editions.collatex.permutations.WitnessBuilder;

@SuppressWarnings("serial")
public class ColorsPage extends WebPage {

  public ColorsPage() {
    ColorsModel model = new ColorsModel("base", "", "", "");
    add(new Label("colorview", new PropertyModel(model, "html")).setEscapeModelStrings(false));
    add(new ColorsForm("alignmentform", model));
    add(new BookmarkablePageLink("usecaselink", UseCasePage.class));
    add(new BookmarkablePageLink("previous", Homepage.class));
  }

}

@SuppressWarnings("serial")
class ColorsModel implements Serializable {

  public String witness1;
  public String witness2;
  public String witness3;
  public String witness4;
  public String html;
  public FileUpload witnessFile1;
  public FileUpload witnessFile2;
  public FileUpload witnessFile3;
  public FileUpload witnessFile4;

  public ColorsModel(String _witness1, String _witness2, String _witness3, String _witness4) {
    this.witness1 = _witness1;
    this.witness2 = _witness2;
    this.witness3 = _witness3;
    this.witness4 = _witness4;
    generateHTML();
  }

  public void generateHTML() {
    List<String> messages = Lists.newArrayList();
    Witness[] witnesses = getWitnesses(messages);
    //    Util.p(witnesses);
    CollateCore colors = new CollateCore(witnesses);
    ColorsView colorsView = new ColorsView(colors);
    colorsView.addMessages(messages);
    this.html = colorsView.toHtml();

  }

  private void add(String witness, FileUpload witnessFile, List<Witness> witnesses, List<String> messages) {
    if (witnessFile != null) {
      try {
        witnesses.add(new WitnessBuilder().build(witnessFile.getInputStream()));
      } catch (SAXException e) {
        messages.add("Invalid file: " + witnessFile.getClientFileName());
      } catch (IOException e) {
        messages.add("Could not upload the file: " + witnessFile.getClientFileName());
      }
    } else {
      if (!witness.isEmpty()) {
        witnesses.add(new WitnessBuilder().build(witness));
      }
    }
  }

  public Witness[] getWitnesses(List<String> messages) {
    List<Witness> witnesses = Lists.newArrayList();
    add(witness1, witnessFile1, witnesses, messages);
    add(witness2, witnessFile2, witnesses, messages);
    add(witness3, witnessFile3, witnesses, messages);
    add(witness4, witnessFile4, witnesses, messages);
    witnessFile1 = null;
    witnessFile2 = null;
    witnessFile3 = null;
    witnessFile4 = null;

    return witnesses.toArray(new Witness[0]);
  }
}

@SuppressWarnings("serial")
class ColorsForm extends Form {
  private final ColorsModel modelForView;

  public ColorsForm(String id, ColorsModel myModelForView) {
    super(id);
    this.modelForView = myModelForView;
    add(new TextField("witness1", new PropertyModel(modelForView, "witness1")).setConvertEmptyInputStringToNull(false));
    add(new TextField("witness2", new PropertyModel(modelForView, "witness2")).setConvertEmptyInputStringToNull(false));
    add(new TextField("witness3", new PropertyModel(modelForView, "witness3")).setConvertEmptyInputStringToNull(false));
    add(new TextField("witness4", new PropertyModel(modelForView, "witness4")).setConvertEmptyInputStringToNull(false));
    add(new FileUploadField("witnessFile1", new PropertyModel(modelForView, "witnessFile1")));
    add(new FileUploadField("witnessFile2", new PropertyModel(modelForView, "witnessFile2")));
    add(new FileUploadField("witnessFile3", new PropertyModel(modelForView, "witnessFile3")));
    add(new FileUploadField("witnessFile4", new PropertyModel(modelForView, "witnessFile4")));

  }

  @Override
  protected void onSubmit() {
    //    modelForView.uploadFiles();
    modelForView.generateHTML();
  }
}
