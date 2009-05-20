package eu.interedition.collatex.superbase;

import java.util.List;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.permutations.Witness;
import com.sd_editions.collatex.permutations.Word;


public class Superbase extends Witness {
  private final List<Column> columnForEachWord;

  public Superbase() {
    this.columnForEachWord = Lists.newArrayList();
  }

  public void addWord(Word word, Column column) {
    getWords().add(word);
    columnForEachWord.add(column);
  }

  public Column getColumnFor(Word word) {
    int indexOf = getWords().indexOf(word);
    Column column = columnForEachWord.get(indexOf);
    if (column == null) {
      throw new RuntimeException(word.toString() + " not in alignment table!");
    }
    return column;
  }

  //  public void setColumn(int position, Column column) {
  //    columnForEachWord.set(position - 1, column);
  //  }
}
