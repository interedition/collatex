package eu.interedition.collatex2.implementation.vg_alignment;

import eu.interedition.collatex2.implementation.vg_analysis.Analysis;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface IVariantGraphListener {

  void newSuperbase(IWitness superbase);

  void newLinkedTokenMap(VariantGraph graph, IWitness witness, Map<INormalizedToken, INormalizedToken> tokenMap);

  void newAlignment(Alignment alignment);

  void newAnalysis(Analysis analysis);
}
