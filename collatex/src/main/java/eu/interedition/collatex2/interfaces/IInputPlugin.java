package eu.interedition.collatex2.interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public interface IInputPlugin {
  void registerInputPlugin();
  List<IWitness> readFile() throws FileNotFoundException, IOException;
}
