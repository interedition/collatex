package eu.interedition.collatex.medite;

import java.util.TreeSet;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
class Phrase<T extends Match> extends TreeSet<T> implements Comparable<Phrase<T>> {

  @Override
  public int compareTo(Phrase<T> o) {
    return first().compareTo(o.first());
  }
}
