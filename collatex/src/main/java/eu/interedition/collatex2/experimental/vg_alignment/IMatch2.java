package eu.interedition.collatex2.experimental.vg_alignment;

import eu.interedition.collatex2.interfaces.IPhrase;

public interface IMatch2 {

  String getNormalized();
  
  IPhrase getPhraseA();
  
  IPhrase getPhraseB();

}
