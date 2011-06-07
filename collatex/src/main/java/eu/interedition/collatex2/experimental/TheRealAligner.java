package eu.interedition.collatex2.experimental;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.vg_analysis.Analysis;
import eu.interedition.collatex2.implementation.vg_analysis.ISequence;
import eu.interedition.collatex2.implementation.vg_analysis.ITransposition2;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class TheRealAligner {
  private Analysis analysis;

  public Map<INormalizedToken, INormalizedToken> align(IWitness superbase, IWitness witness) {
    // 1. Do the matching and linking of tokens
    MyNewLinker linker = new MyNewLinker();
    Map<INormalizedToken, INormalizedToken> linkedTokens = linker.link2(superbase, witness);
    // 2. Determine sequences
    SequenceDetection3 detection = new SequenceDetection3();
    List<ISequence> sequences = detection.getSequences(linkedTokens, superbase, witness);
    // 3. Determine transpositions of the sequences
    this.analysis = new Analysis(sequences, superbase); 
    List<ITransposition2> transpositions = analysis.getTranspositions();
    // 4. Determine the actual alignment
    return determineAlignedTokens(linkedTokens, transpositions, witness);
  }

  public Analysis getAnalysis() {
    return analysis;
  }
  
  private Map<INormalizedToken, INormalizedToken> determineAlignedTokens(Map<INormalizedToken, INormalizedToken> linkedTokens, List<ITransposition2> transpositions, IWitness witness) {
    Map<INormalizedToken, INormalizedToken> alignedTokens = Maps.newLinkedHashMap();
    alignedTokens.putAll(linkedTokens);
    List<ISequence> sequencesThatAreTransposed = getSequencesThatAreTransposed(transpositions, witness);
    for (ISequence sequenceA : sequencesThatAreTransposed) {
      for (INormalizedToken token : sequenceA.getWitnessPhrase().getTokens()) {
        alignedTokens.remove(token);
      }
    }
    return alignedTokens;
  }

  // NOTE: this method should not return the original sequence when a mirror exists!
  private List<ISequence> getSequencesThatAreTransposed(List<ITransposition2> transpositions, IWitness witness) {
    List<ISequence> transposedSequences = Lists.newArrayList();
    final Stack<ITransposition2> transToCheck = new Stack<ITransposition2>();
    transToCheck.addAll(transpositions);
    Collections.reverse(transToCheck);
    while (!transToCheck.isEmpty()) {
      final ITransposition2 top = transToCheck.pop();
      // System.out.println("Detected transposition: "+top.getSequenceA().toString());
      final ITransposition2 mirrored = findMirroredTransposition(transToCheck, top);
      // remove mirrored transpositions (a->b, b->a) from transpositions
      if (mirrored != null && transpositionsAreNear(top, mirrored, witness)) {
        // System.out.println("Detected mirror: "+mirrored.getSequenceA().toString());
        // System.out.println("Keeping: transposition " + top.toString());
        // System.out.println("Removing: transposition " + mirrored.toString());
        transToCheck.remove(mirrored);
        transposedSequences.add(mirrored.getSequenceA());
      } else {
        transposedSequences.add(top.getSequenceA());
      }
    }
    return transposedSequences;
  }

  // Note: this only calculates the distance between the tokens in the witness.
  // Note: it does not take into account a possible distance in the vertices in the graph!
  private boolean transpositionsAreNear(ITransposition2 top, ITransposition2 mirrored, IWitness witness) {
    INormalizedToken lastToken = top.getSequenceB().getWitnessPhrase().getLastToken();
    INormalizedToken firstToken = mirrored.getSequenceB().getWitnessPhrase().getFirstToken();
    return witness.isNear(lastToken, firstToken);
  }

  //NOTE: It would be better to not use getNormalized here!
  //NOTE: This does not work with a custom matching function
  private static ITransposition2 findMirroredTransposition(final Stack<ITransposition2> transToCheck, final ITransposition2 original) {
    for (final ITransposition2 transposition : transToCheck) {
      if (transposition.getSequenceA().getNormalized().equals(original.getSequenceB().getNormalized())) {
        if (transposition.getSequenceB().getNormalized().equals(original.getSequenceA().getNormalized())) {
          return transposition;
        }
      }
    }
    return null;
  }


}
