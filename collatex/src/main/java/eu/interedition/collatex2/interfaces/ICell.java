package eu.interedition.collatex2.interfaces;

public interface ICell {
  
  boolean isEmpty();
  
  INormalizedToken getToken();
  
  int getPosition();
  
}
