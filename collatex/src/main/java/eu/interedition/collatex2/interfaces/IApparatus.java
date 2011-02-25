package eu.interedition.collatex2.interfaces;

import java.util.List;

public interface IApparatus {

  List<IApparatusEntry> getEntries();

  List<IWitness> getWitnesses();

}