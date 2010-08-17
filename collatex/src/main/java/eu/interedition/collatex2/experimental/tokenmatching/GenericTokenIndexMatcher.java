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

public class GenericTokenIndexMatcher extends IndexMatcher implements ITokenMatcher {
  private final ITokenContainer table;

  public GenericTokenIndexMatcher(ITokenContainer table) {
    this.table = table;
  }

  public List<ITokenMatch> getMatches(IWitness witness) {
    final List<String> repeatingTokens = combineRepeatingTokens(table, witness);
    IWitnessIndex tableIndex = table.getTokenIndex(repeatingTokens);
    return IndexMatcher.findMatches(tableIndex, new WitnessIndex(witness, repeatingTokens));
  }
  
  //TODO: change return type from List into Set?
  private List<String> combineRepeatingTokens(final ITokenContainer table, final IWitness witness) {
    final Set<String> repeatingTokens = Sets.newHashSet();
    repeatingTokens.addAll(table.findRepeatingTokens());
    repeatingTokens.addAll(witness.findRepeatingTokens());
    return Lists.newArrayList(repeatingTokens);
  }
}
