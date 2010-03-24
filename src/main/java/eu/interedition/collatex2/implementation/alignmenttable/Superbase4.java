package eu.interedition.collatex2.implementation.alignmenttable;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.input.NormalizedToken;
import eu.interedition.collatex2.implementation.input.NormalizedWitness;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.ISuperbase;

public class Superbase4 extends NormalizedWitness implements ISuperbase {
  private final List<IColumn> _columns;

  public Superbase4() {
    super("superbase");
    _columns = Lists.newArrayList();
  }

  public static ISuperbase create(final IAlignmentTable table) {
    final ISuperbase superbase = new Superbase4();
    for (final IColumn column : table.getColumns()) {
      superbase.addColumn(column);
    }
    return superbase;
  }

  public void addColumn(final IColumn column) {
    final List<INormalizedToken> variants = column.getVariants();
    for (final INormalizedToken variant : variants) {
      addToken(variant, column);
    }
  }

  @Override
  public void addToken(final INormalizedToken token, final IColumn column) {
    //NOTE: here we make a new NormalizedToken ... to set the position,  and sigil.
    final int position = size() + 1;
    //TODO: We really need a token.getContent!
    final INormalizedToken faked = new NormalizedToken("superbase", "FAKED", position, token.getNormalized());
    getTokens().add(faked);
    _columns.add(column);
  }

  @Override
  public IColumn getColumnFor(final INormalizedToken tokenA) {
    final int position = tokenA.getPosition();
    final IColumn column = _columns.get(position - 1);
    return column;
  }

  public List<IColumn> getColumnsFor(final IPhrase phraseA) {
    final List<IColumn> columns = Lists.newArrayList();
    for (final INormalizedToken tokenA : phraseA.getTokens()) {
      final IColumn column = getColumnFor(tokenA);
      columns.add(column);
    }
    return columns;
  }

  @Override
  public String toString() {
    String result = "Superbase: (";
    String delimiter = "";
    for (final INormalizedToken t : getTokens()) {
      result += delimiter + t.getNormalized();
      delimiter = ", ";
    }

    result += ")";
    return result;
  }

}
