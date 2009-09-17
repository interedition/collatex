package eu.interedition.collatex.input.visitors;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.Word;

public interface IResourceVisitor {

  public void visitWitnessSet(WitnessSet witnessSet);

  public void visitWitness(Witness witness);

  public void visitWord(Word word);

  public void postVisitWitness(Witness witness);

}
