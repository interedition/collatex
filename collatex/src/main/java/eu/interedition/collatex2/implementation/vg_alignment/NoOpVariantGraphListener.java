package eu.interedition.collatex2.implementation.vg_alignment;

import eu.interedition.collatex2.implementation.vg_analysis.Analysis;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class NoOpVariantGraphListener implements IVariantGraphListener {

  @Override
  public void newSuperbase(IWitness superbase) {
  }

  @Override
  public void newLinkedTokenMap(VariantGraph graph, IWitness witness, Map<INormalizedToken, INormalizedToken> tokenMap) {
  }

  @Override
  public void newAlignment(Alignment alignment) {
  }

  @Override
  public void newAnalysis(Analysis analysis) {
  }

}
