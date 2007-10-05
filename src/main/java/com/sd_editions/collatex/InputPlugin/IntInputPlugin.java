package com.sd_editions.collatex.InputPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;

public interface IntInputPlugin {

  void registerInputPlugin();

  BlockStructure readFile() throws FileNotFoundException,IOException,BlockStructureCascadeException ;
}
