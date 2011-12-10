package eu.interedition.collatex.interfaces;

import java.util.Comparator;
import java.util.Map;


public interface ITokenLinker {

  Map<Token, Token> link(IWitness graph, IWitness b, Comparator<Token> comparator);

}