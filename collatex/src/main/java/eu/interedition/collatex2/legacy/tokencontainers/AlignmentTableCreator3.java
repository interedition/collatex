package eu.interedition.collatex2.legacy.tokencontainers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.experimental.vg_alignment.Alignment2;
import eu.interedition.collatex2.experimental.vg_alignment.IAlignment2;
import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.tokenmatching.TokenIndexMatcher;
import eu.interedition.collatex2.input.Phrase;
import eu.interedition.collatex2.interfaces.IAddition;
import eu.interedition.collatex2.interfaces.IAligner;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.ICallback;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IInternalColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IReplacement;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.ITransposition;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.legacy.alignment.Alignment;
import eu.interedition.collatex2.legacy.alignment.SequenceDetection;
import eu.interedition.collatex2.legacy.alignmenttable.Column3;
import eu.interedition.collatex2.legacy.alignmenttable.Columns;
import eu.interedition.collatex2.legacy.tokenmatching.ColumnPhraseMatch;
import eu.interedition.collatex2.todo.gapdetection.Gap;
import eu.interedition.collatex2.todo.gapdetection.GapDetection;

public class AlignmentTableCreator3 implements IAligner {

  private IAlignmentTable alignmentTable;
  private ICallback callback = new ICallback() {
    @Override
    public void alignment(final IAlignment alignment) {
    }
  };

  public AlignmentTableCreator3(CollateXEngine engine) {
    alignmentTable = engine.createAlignmentTable();
  }

  @Override
  public void setCallback(ICallback callback) {
    this.callback = callback;
  }

  @Override
  public IAligner add(IWitness... witnesses) {
    for (IWitness witness : witnesses) {
      final boolean tableIsEmpty = alignmentTable.getSigla().isEmpty();
      alignmentTable.getSigla().add(witness.getSigil());
      if (tableIsEmpty) {
        for (final INormalizedToken token : witness.getTokens()) {
          alignmentTable.add(new Column3(token, alignmentTable.size() + 1));
        }
        continue;
      }
      IAlignment alignment = oldStyleAnalyse(alignmentTable, witness);
      callback.alignment(alignment);
      final IAlignment alignment3 = makeAddDelFromTrans(alignmentTable, alignment);
      addMatchesToAlignmentTable(alignment3);
      addReplacementsToAlignmentTable(alignmentTable, alignment3);
      addAdditionsToAlignmentTable(alignmentTable, alignment3);      
    }
    return this;
  }

  public static IAlignment oldStyleAnalyse(IAlignmentTable alignmentTable, IWitness witness) {
    // Map base tokens to IColumn
    Map<INormalizedToken, IInternalColumn> baseTokenToColumn = Maps.newLinkedHashMap();
    for (IColumn col : alignmentTable.getColumns()) {
      for (String sigil : col.getInternalColumn().getSigla()) {
        INormalizedToken baseToken = col.getInternalColumn().getToken(sigil);
        baseTokenToColumn.put(baseToken, col.getInternalColumn());
      }
    }

    final IAlignment2 alignment_without_seq = AlignmentTableCreator3.createAlignmentUsingIndex(alignmentTable, witness);
    List<ITokenMatch> matches1 = alignment_without_seq.getTokenMatches();
    // Convert matches to legacy
    List<IMatch> result = Lists.newArrayList();
    for (ITokenMatch match : matches1) {
      INormalizedToken base = match.getBaseToken();
      INormalizedToken witnessT = match.getWitnessToken();
      IInternalColumn column = baseTokenToColumn.get(base);
      IColumns columns = new Columns(Lists.newArrayList(column));
      IPhrase phrase = new Phrase(Lists.newArrayList(witnessT));
      IMatch columnMatch = new ColumnPhraseMatch(columns, phrase);
      result.add(columnMatch);
    }
    // System.out.println("!!"+result);
    // Order results based on position in table
    List<IMatch> ordered = Lists.newArrayList(result);
    Comparator<? super IMatch> c = new Comparator<IMatch>() {
    
      @Override
      public int compare(IMatch o1, IMatch o2) {
        return o1.getColumns().getBeginPosition() - o2.getColumns().getBeginPosition();
      }
    
    };
    Collections.sort(ordered, c);
    final List<IMatch> matches = ordered;
    // OLD STYLE ANALYSIS FOLLOWS!
    final List<IGap> gaps = GapDetection.detectGap(matches, alignmentTable, witness);
    final Alignment alignment2 = new Alignment(matches, gaps);

    final IAlignment alignment = SequenceDetection.improveAlignment(alignment2);
    return alignment;
  }
  
  @Override
  public IAlignmentTable getResult() {
    return alignmentTable;
  }

  // TODO: rename this method!
  public static IAlignment2 createAlignmentUsingIndex(final IAlignmentTable table, final IWitness witness) {
    // Do the token matching
    TokenIndexMatcher matcher = new TokenIndexMatcher(table);
    List<ITokenMatch> matches1 = matcher.getMatches(witness);
    return new Alignment2(matches1);
  }

  private static void addMatchesToAlignmentTable(final IAlignment alignment) {
    final List<IMatch> matches = alignment.getMatches();
    for (final IMatch match : matches) {
      addMatchToAlignmentTable(match);
    }
  }

  private static void addReplacementsToAlignmentTable(final IAlignmentTable table, final IAlignment alignment) {
    final List<IReplacement> replacements = alignment.getReplacements();
    for (final IReplacement replacement : replacements) {
      table.addReplacement(replacement);
    }
  }

  private static void addMatchToAlignmentTable(final IMatch match) {
    final IColumns columns = match.getColumns();
    columns.addMatchPhrase(match.getPhrase());
  }

  private static void addAdditionsToAlignmentTable(final IAlignmentTable table, final IAlignment alignment) {
    final List<IAddition> additions = alignment.getAdditions();
    for (final IAddition addition : additions) {
      table.addAddition(addition);
    }
  }

  // NOTE: the way transpositions are handled here
  // and the way there are placed in the alignment table
  // is just one specific case, namely
  // a b
  // b a
  // this becomes |a| b| |, | |b|a|
  public static IAlignment makeAddDelFromTrans(IAlignmentTable table, final IAlignment alignment) {
    // handle transpositions here!
    final List<ITransposition> transpositions = alignment.getTranspositions();
    final List<IMatch> matches = removeMatchesFromTranpositions(alignment, transpositions);
    final Stack<ITransposition> transToCheck = new Stack<ITransposition>();
    final List<IGap> gaps = alignment.getGaps();
    transToCheck.addAll(transpositions);
    Collections.reverse(transToCheck);
    while (!transToCheck.isEmpty()) {
      final ITransposition top = transToCheck.pop();
      final ITransposition mirrored = findMirroredTransposition(transToCheck, top);
      // Note: this only calculates the distance between the columns.
      // Note: it does not take into account a possible distance in the phrases!
      if (mirrored != null && distanceBetweenTranspositions(top, mirrored) == 0) {
        // System.out.println("Keeping: transposition " + top.toString());
        // System.out.println("Removing: transposition " + mirrored.toString());
        // remove mirrored transpositions (a->b, b->a) from transpositions
        transToCheck.remove(mirrored);
        final IGap addition = makeAdditionOutOfTransposition(top);
        gaps.add(addition);
        matches.add(top.getMatchA());
      } else {
        final IGap replacement = makeReplacementOutOfTransposition(top, table);
        gaps.add(replacement);
      }
    }

    final IAlignment al = new Alignment(matches, gaps);
    return al;
  }

  private static IGap makeReplacementOutOfTransposition(final ITransposition top, IAlignmentTable table) {
    final IColumns columns = top.getMatchA().getColumns();
    final IPhrase phrase = top.getMatchB().getPhrase();
    final IInternalColumn lastColumn = columns.getLastColumn();
    final IInternalColumn nextColumn;
    if (lastColumn.getPosition() == table.size()){
      nextColumn = null;
    } else {
      nextColumn = table.getColumns().get(lastColumn.getPosition()).getInternalColumn();
    }
    final IGap gap = new Gap(columns, phrase, nextColumn);
    return gap;
  }

  private static int distanceBetweenTranspositions(final ITransposition top, final ITransposition mirrored) {
    final int beginPosition = mirrored.getMatchA().getColumns().getBeginPosition();
    final int endPosition = top.getMatchA().getColumns().getEndPosition();
    // System.out.println(beginPosition + ":" + endPosition);
    final int distance = beginPosition - (endPosition + 1);
    // System.out.println(distance);
    return distance;
  }

  private static List<IMatch> removeMatchesFromTranpositions(final IAlignment alignment, final List<ITransposition> ntranspositions) {
    // remove matches from transpositions
    final List<IMatch> matches = alignment.getMatches();
    for (final ITransposition t : ntranspositions) {
      final IMatch witness = t.getMatchB();
      // Note: this is not nice; this removes from the original list!
      matches.remove(witness);
    }
    return matches;
  }

  private static IGap makeAdditionOutOfTransposition(final ITransposition t) {
    // make an addition from the match
    final IMatch witness = t.getMatchB();
    final IColumns columns = new Columns();
    final IPhrase phrase = witness.getPhrase();
    // NOTE: I need the next column here!
    // NOTE: so the next column should be in the transposition!
    // TODO add next column to addition, transposition, etc
    // TODO and use that!
    final IInternalColumn nextColumn = t.getMatchA().getColumns().getFirstColumn();
    final IGap addition = new Gap(columns, phrase, nextColumn);
    // Note: this is not nice; this adds to the original list!
    return addition;
  }

  private static ITransposition findMirroredTransposition(final Stack<ITransposition> transToCheck, final ITransposition original) {
    for (final ITransposition transposition : transToCheck) {
      if (transposition.getMatchA().getNormalized().equals(original.getMatchB().getNormalized())) {
        if (transposition.getMatchB().getNormalized().equals(original.getMatchA().getNormalized())) {
          return transposition;
        }
      }
    }
    return null;
  }
}
