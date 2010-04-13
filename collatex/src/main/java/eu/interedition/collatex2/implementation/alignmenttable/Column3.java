package eu.interedition.collatex2.implementation.alignmenttable;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import com.google.common.base.Join;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.interfaces.ColumnState;
import eu.interedition.collatex2.interfaces.IAlignmentTableVisitor;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class Column3 implements IColumn {
  private final Map<String, INormalizedToken> sigliToTokens;
  private final List<INormalizedToken> variants;
  private int _position;
  private ColumnState state;

  public Column3(final INormalizedToken token, final int position) {
    _position = position;
    this.sigliToTokens = Maps.newHashMap();
    this.variants = Lists.newArrayList();
    init(token);
  }

  private void init(final INormalizedToken token) {
    sigliToTokens.put(token.getSigil(), token);
    variants.add(token);
    state = ColumnState.NEW;
  }

  @Override
  public boolean containsWitness(final String sigil) {
    return sigliToTokens.containsKey(sigil);
  }

  @Override
  public INormalizedToken getToken(final String sigil) {
    if (!containsWitness(sigil)) {
      throw new NoSuchElementException("Witness " + sigil + " is not present in this column");
    }
    return sigliToTokens.get(sigil);
  }

  @Override
  public List<INormalizedToken> getVariants() {
    return variants;
  }

  @Override
  public void addVariant(final INormalizedToken token) {
    sigliToTokens.put(token.getSigil(), token);
    variants.add(token);
    state = state.addVariant();
  }

  @Override
  public void addMatch(final INormalizedToken token) {
    sigliToTokens.put(token.getSigil(), token);
    state = state.addMatch();
  }

  @Override
  public int getPosition() {
    return _position;
  }

  @Override
  public void setPosition(final int position) {
    _position = position;
  }

  @Override
  public ColumnState getState() {
    return state;
  }

  @Override
  public int compareTo(final IColumn other) {
    return (getPosition() - other.getPosition());
  }

  @Override
  public String toString() {
    final List<String> list = Lists.newArrayList();
    for (final Entry<String, INormalizedToken> entry : sigliToTokens.entrySet()) {
      list.add(//
          new StringBuilder(entry.getKey()).//
              append(":").//
              append(entry.getValue().getPosition()).//
              append("->").//
              append(entry.getValue().getContent()).//
              toString()//
          );
    }
    return Join.join(",", list);
  }

  @Override
  public List<String> getSigli() {
    return Lists.newArrayList(sigliToTokens.keySet());
  }

  public void accept(final IAlignmentTableVisitor visitor) {
    visitor.visitColumn(this);
    final List<String> sigli = this.getSigli();
    for (final String sigel : sigli) {
      final INormalizedToken token = getToken(sigel);
      visitor.visitToken(sigel, token);
    }
  }

}
