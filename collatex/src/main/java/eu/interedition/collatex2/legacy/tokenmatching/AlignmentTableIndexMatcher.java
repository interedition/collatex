package eu.interedition.collatex2.legacy.tokenmatching;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.experimental.tokenmatching.ITokenMatcher;
import eu.interedition.collatex2.experimental.tokenmatching.IndexMatcher;
import eu.interedition.collatex2.implementation.indexing.WitnessIndex;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;
import eu.interedition.collatex2.legacy.tokencontainers.AlignmentTableIndex;

//TODO: LEGACY CLASS REMOVE ! REMOVE !
public class AlignmentTableIndexMatcher extends IndexMatcher implements ITokenMatcher {
  private final IAlignmentTable table;
  private IWitnessIndex alignmentTableIndex;

  public AlignmentTableIndexMatcher(IAlignmentTable table) {
    this.table = table;
  }

  public List<ITokenMatch> getMatches(IWitness witness) {
    final List<String> repeatingTokens = combineRepeatingTokens(table, witness);
    alignmentTableIndex = AlignmentTableIndex.create(table, repeatingTokens);
    return IndexMatcher.findMatches(alignmentTableIndex, new WitnessIndex(witness, repeatingTokens));
  }

  private List<String> combineRepeatingTokens(final IAlignmentTable table, final IWitness witness) {
    final Set<String> repeatingTokens = Sets.newHashSet();
    repeatingTokens.addAll(table.findRepeatingTokens());
    repeatingTokens.addAll(witness.findRepeatingTokens());
    return Lists.newArrayList(repeatingTokens);
  }
}
