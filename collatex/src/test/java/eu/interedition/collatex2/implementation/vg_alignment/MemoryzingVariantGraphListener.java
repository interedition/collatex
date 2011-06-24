package eu.interedition.collatex2.implementation.vg_alignment;

import eu.interedition.collatex2.implementation.vg_analysis.Analysis;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class MemoryzingVariantGraphListener implements IVariantGraphListener {

  private IWitness superbase;
  private Map<INormalizedToken, INormalizedToken> linkedTokenMap;
  private Alignment alignment;
  private Analysis analysis;

  public IWitness getSuperbase() {
    return superbase;
  }

  public Map<INormalizedToken, INormalizedToken> getLinkedTokenMap() {
    return linkedTokenMap;
  }

  public Alignment getAlignment() {
    return alignment;
  }

  public Analysis getAnalysis() {
    return analysis;
  }

  @Override
  public void newSuperbase(IWitness superbase) {
    this.superbase = superbase;
  }

  @Override
  public void newLinkedTokenMap(VariantGraph graph, IWitness witness, Map<INormalizedToken, INormalizedToken> tokenMap) {
    this.linkedTokenMap = tokenMap;
  }

  @Override
  public void newAlignment(Alignment alignment) {
    this.alignment = alignment;
  }

  @Override
  public void newAnalysis(Analysis analysis) {
    this.analysis = analysis;
  }
}
