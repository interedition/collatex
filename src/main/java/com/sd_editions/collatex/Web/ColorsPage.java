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
import com.sd_editions.collatex.permutations.Witness;
import com.sd_editions.collatex.permutations.WitnessBuilder;
import com.sd_editions.collatex.permutations.WitnessBuilder.ContentType;

@SuppressWarnings("serial")
public class ColorsPage extends WebPage {

  public ColorsPage() {
    ColorsModel model = new ColorsModel("", "", "", "");
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
  public transient FileUpload witnessFile1;
  public transient FileUpload witnessFile2;
  public transient FileUpload witnessFile3;
  public transient FileUpload witnessFile4;

  public ColorsModel(String _witness1, String _witness2, String _witness3, String _witness4) {
    this.witness1 = _witness1;
    this.witness2 = _witness2;
    this.witness3 = _witness3;
    this.witness4 = _witness4;
    generateHTML();
  }

  public void generateHTML() {
    this.html = getView().toHtml();

  }

  private void add(String witness, FileUpload witnessFile, List<Witness> witnesses, List<String> messages) {
    if (witnessFile != null) {
      try {
        ContentType type = WitnessBuilder.ContentType.value(witnessFile.getContentType());
        if (type != null) {
          witnesses.add(new WitnessBuilder().build(witnessFile.getInputStream(), type));
        } else {
          messages.add("Invalid content type of file: " + witnessFile.getClientFileName());
        }
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

  public ColorsView getView() {
    List<String> messages = Lists.newArrayList();
    List<Witness> witnesses = Lists.newArrayList();
    add(witness1, witnessFile1, witnesses, messages);
    add(witness2, witnessFile2, witnesses, messages);
    add(witness3, witnessFile3, witnesses, messages);
    add(witness4, witnessFile4, witnesses, messages);

    return new ColorsView(messages, witnesses);
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
    modelForView.generateHTML();
  }
}
