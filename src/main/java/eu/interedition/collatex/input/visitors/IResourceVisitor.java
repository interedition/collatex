package eu.interedition.collatex.input.visitors;

import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.Word;

public interface IResourceVisitor {

  public void visitWitnessSet(WitnessSet witnessSet);

  public void visitSegment(Segment witness);

  public void visitWord(Word word);

  // TODO: rename!
  // TODO: visitWitness
  public void postVisitWitness(Segment witness);

}
