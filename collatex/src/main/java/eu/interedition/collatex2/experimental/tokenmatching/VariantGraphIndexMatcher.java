package eu.interedition.collatex2.experimental.tokenmatching;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.implementation.indexing.WitnessIndex;
import eu.interedition.collatex2.interfaces.ITokenContainer;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

public class VariantGraphIndexMatcher extends IndexMatcher implements ITokenMatcher {
  private final ITokenContainer base;

  public VariantGraphIndexMatcher(ITokenContainer base) {
    this.base = base;
  }

  public List<ITokenMatch> getMatches(IWitness witness) {
    final List<String> repeatedTokens = combineRepeatedTokens(base, witness);
    IWitnessIndex basseIndex = base.getTokenIndex(repeatedTokens);
    return IndexMatcher.findMatches(basseIndex, new WitnessIndex(witness, repeatedTokens));
  }
  
  //TODO: change return type from List into Set?
  private List<String> combineRepeatedTokens(final ITokenContainer table, final IWitness witness) {
    final Set<String> repeatedTokens = Sets.newHashSet();
    repeatedTokens.addAll(table.findRepeatingTokens());
    repeatedTokens.addAll(witness.findRepeatingTokens());
    return Lists.newArrayList(repeatedTokens);
  }


}
