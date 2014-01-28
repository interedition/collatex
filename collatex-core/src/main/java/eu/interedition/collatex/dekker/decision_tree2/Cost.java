package eu.interedition.collatex.dekker.decision_tree2;

/*
 * Generic cost value object for use with the a* algorithm.
 * 
 * @author: Ronald Haentjens Dekker
 */
public abstract class Cost<T extends Cost<T>> implements Comparable<T> {

  abstract T plus(T other);
  
}
