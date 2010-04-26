package eu.interedition.collatex2.implementation.matching;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.implementation.indexing.AlignmentTableIndex;
import eu.interedition.collatex2.implementation.indexing.WitnessIndex;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTableIndex;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableIndexMatcher {
  private final IAlignmentTable table;
  private final IWitness witness;
  private IAlignmentTableIndex alignmentTableIndex;

  public AlignmentTableIndexMatcher(IAlignmentTable table, IWitness witness) {
    this.table = table;
    this.witness = witness;
  }

  public List<ITokenMatch> getMatches() {
    final List<String> repeatingTokens = combineRepeatingTokens(table, witness);
    alignmentTableIndex = AlignmentTableIndex.create(table, repeatingTokens);
    return IndexMatcher.findMatches(alignmentTableIndex, new WitnessIndex(witness, repeatingTokens));
  }

  public List<String> combineRepeatingTokens(final IAlignmentTable table, final IWitness witness) {
    final Set<String> repeatingTokens = Sets.newHashSet();
    repeatingTokens.addAll(table.findRepeatingTokens());
    repeatingTokens.addAll(witness.findRepeatingTokens());
    return Lists.newArrayList(repeatingTokens);
  }

  public IAlignmentTableIndex getAlignmentTableIndex() {
    return alignmentTableIndex;
  }


}
