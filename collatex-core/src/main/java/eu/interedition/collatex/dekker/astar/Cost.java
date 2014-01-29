package eu.interedition.collatex.dekker.astar;

/*
 * Generic cost value object for use with the a* algorithm.
 * 
 * @author: Ronald Haentjens Dekker
 */
public abstract class Cost<T extends Cost<T>> implements Comparable<T> {

  protected abstract T plus(T other);
  
}
