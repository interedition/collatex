package eu.interedition.collatex.input.visitors;

import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.Word;

public interface IResourceVisitor {

  public void visitWitnessSet(WitnessSet witnessSet);

  public void visitWitness(Segment witness);

  public void visitWord(Word word);

  public void postVisitWitness(Segment witness);

}
