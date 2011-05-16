package eu.interedition.collatex2.experimental;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.vg_analysis.Analysis;
import eu.interedition.collatex2.implementation.vg_analysis.ISequence;
import eu.interedition.collatex2.implementation.vg_analysis.ITransposition2;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class TheRealAligner {
  private Analysis analysis;

  public Map<INormalizedToken, INormalizedToken> align(IWitness superbase, IWitness witness) {
    MyNewLinker linker = new MyNewLinker();
    Map<INormalizedToken, INormalizedToken> linkedTokens = linker.link(superbase, witness);
    // maak hier nu de match sequences
    SequenceDetection3 detection = new SequenceDetection3();
    List<ISequence> sequences = detection.getSequences(linkedTokens, superbase, witness);
    //System.out.println(sequences);
    // daarna transposition detection
    this.analysis = new Analysis(sequences, superbase); 
    // dan hebben we genoeg info voor de alignment
    Map<INormalizedToken, INormalizedToken> alignedTokens = Maps.newLinkedHashMap();
    alignedTokens.putAll(linkedTokens);
    //TODO: kijk hier naar de transpositions en de afstand daartussen
    //TODO: transposed tokens are removed for now, this is not 100% correct
    List<ITransposition2> transpositions = analysis.getTranspositions();
    for (ITransposition2 transposition : transpositions) {
      ISequence sequenceA = transposition.getSequenceA();
      for (INormalizedToken token : sequenceA.getWitnessPhrase().getTokens()) {
        alignedTokens.remove(token);
      }
    }
    return alignedTokens;
  }

  public Analysis getAnalysis() {
    return analysis;
  }

}
