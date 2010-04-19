/**
 * 
 */
package eu.interedition.collatex2.implementation.matching;

import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class ColumnToken {
  IColumn column;
  INormalizedToken token;

  public ColumnToken(IColumn column, INormalizedToken token) {
    this.column = column;
    this.token = token;
  }
}