package eu.interedition.collatex.input.visitors;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.Word;

// import org.json.*;

// TODO: use java Json package to split off actual json rendering
// TODO: extract AbstractResourceVisitor
public class JSonVisitor implements IResourceVisitor {

  private final StringBuilder builder;
  private String witnessSplitter;

  public JSonVisitor() {
    builder = new StringBuilder();
  }

  // TODO: add Witness ID!
  @Override
  public void visitWitness(Witness witness) {
    // Note: this way is not really pretty
    // with the constant assignments, but it saves
    // if statements
    // an alternative would be to collect all the words
    // in a buffer and then join them in a post method
    witnessSplitter = "";
    builder.append("{ words: [");
  }

  public void postVisitWitness(Witness witness) {
    builder.append("] }");
  }

  @Override
  public void visitWitnessSet(WitnessSet witnessSet) {
  // TODO Auto-generated method stub

  }

  // TODO: add punctuation!
  @Override
  public void visitWord(Word word) {
    builder.append(witnessSplitter);
    builder.append("{ content:");
    builder.append(word.original);
    builder.append("}");
    witnessSplitter = ", ";
  }

  public String getResult() {
    return builder.toString();
  }
}
