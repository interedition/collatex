package eu.interedition.collatex2.interfaces;

import java.util.List;


public interface IApparatusEntry {

  //Note: an empty cell returns an empty phrase!
  //Note: rename to getReading(witness)?
  IPhrase getPhrase(IWitness witness);
  
  //Note: return true means that a reading is not empty!
  //TODO: rename to isEmptyReading() ? --> switch boolean result then!
  boolean containsWitness(IWitness witness);

  List<IWitness> getWitnesses();

  boolean hasEmptyCells();

  ApparatusEntryState getState();

}