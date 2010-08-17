package eu.interedition.collatex2.legacy.tokenmatching;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.experimental.tokenmatching.ITokenMatcher;
import eu.interedition.collatex2.experimental.tokenmatching.IndexMatcher;
import eu.interedition.collatex2.implementation.alignmenttable.Columns;
import eu.interedition.collatex2.implementation.indexing.AlignmentTableIndex;
import eu.interedition.collatex2.implementation.indexing.WitnessIndex;
import eu.interedition.collatex2.input.Phrase;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IAlignmentTableIndex;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IWitness;

//TODO: LEGACY CLASS REMOVE ! REMOVE !
public class AlignmentTableIndexMatcher extends IndexMatcher implements ITokenMatcher {
  private final IAlignmentTable table;
  private IAlignmentTableIndex alignmentTableIndex;

  public AlignmentTableIndexMatcher(IAlignmentTable table) {
    this.table = table;
  }

  public List<ITokenMatch> getMatches(IWitness witness) {
    final List<String> repeatingTokens = combineRepeatingTokens(table, witness);
    alignmentTableIndex = AlignmentTableIndex.create(table, repeatingTokens);
    return IndexMatcher.findMatches(alignmentTableIndex, new WitnessIndex(witness, repeatingTokens));
  }

  public List<IMatch> getColumnMatches(IWitness witness) {
    List<ITokenMatch> tokenMatches = getMatches(witness);
    List<IMatch> columnMatches = Lists.newArrayList();
    for (ITokenMatch tokenMatch : tokenMatches) {
      INormalizedToken tableToken = tokenMatch.getTableToken();
      IColumn column = alignmentTableIndex.getColumn(tableToken);
      IPhrase witnessPhrase = new Phrase(Lists.newArrayList(tokenMatch.getWitnessToken()));
      IColumns columns = new Columns(Lists.newArrayList(column));
      IMatch columnMatch = new ColumnPhraseMatch(columns, witnessPhrase);
      columnMatches.add(columnMatch);
    }
    // Sort the ColumnMatches here
    // otherwise the gapdetection goes ballistic!
    Collections.sort(columnMatches, new Comparator<IMatch>() {
      @Override
      public int compare(IMatch o1, IMatch o2) {
        return o1.getColumns().getBeginPosition() - o2.getColumns().getBeginPosition();
      }
    });
    return columnMatches;
  }
  
  private List<String> combineRepeatingTokens(final IAlignmentTable table, final IWitness witness) {
    final Set<String> repeatingTokens = Sets.newHashSet();
    repeatingTokens.addAll(table.findRepeatingTokens());
    repeatingTokens.addAll(witness.findRepeatingTokens());
    return Lists.newArrayList(repeatingTokens);
  }
}
