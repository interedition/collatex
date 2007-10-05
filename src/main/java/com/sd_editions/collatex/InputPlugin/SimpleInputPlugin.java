package com.sd_editions.collatex.InputPlugin;

import java.io.FileReader;
import java.io.StreamTokenizer;
import java.io.FileNotFoundException;
import java.io.IOException;
import com.sd_editions.collatex.Block.*;

/**
 * An example plugin this simple reading in a text file and breaks down each
 * word (a word being defined as anything between a non-spacing character).
 *
 */

public class SimpleInputPlugin implements IntInputPlugin {

  private String filename;

  public SimpleInputPlugin() {
	this.filename = null;
  }

  public SimpleInputPlugin(String filename) {
	this.filename = filename;
  }

  public void registerInputPlugin() {
	//Do nothing for the moment
  }

  public BlockStructure readFile() throws FileNotFoundException,IOException,BlockStructureCascadeException {
	//Open up the file
	FileReader is = new FileReader(this.filename);
	StreamTokenizer st = new StreamTokenizer(is);
	st.eolIsSignificant(true);

	BlockStructure document = new BlockStructure();
	int token = st.nextToken();
	//Assume we start on line 1
	int lineCount = 1;
	Line pLine = new Line(lineCount);
	document.setRootBlock(pLine, true);
	while (token != StreamTokenizer.TT_EOF) {
	  switch (token) {
		case StreamTokenizer.TT_EOL:
		  //Found an end of line
		  lineCount++;
		  Line nLine = new Line(lineCount);
		  document.setNextSibling(pLine, nLine);
		  pLine = nLine;
		  break;
		case StreamTokenizer.TT_WORD:
		  //Found a work, add it to our block
		  Word word = new Word(st.sval);
		  //This add the word to the last sibling of the line.
		  document.setChildBlock(pLine, word);
		  break;
	  }
	  token = st.nextToken();
	}
	//Close the file
	is.close();
	return document;
  }


  public static void main(String[] args) {
	String filename = "examples/inputfiles/example1.txt";
	BlockStructure document;
	if (args.length==1) {
	  filename = args[0];
	}
	SimpleInputPlugin ip = new SimpleInputPlugin(filename);
	try {
	  document = ip.readFile();
	  System.out.println(document);
	} catch (FileNotFoundException e) {
	  System.out.println("Could not find example file");
	} catch (IOException e) {
	  System.out.println("Could not read example file");
	} catch (BlockStructureCascadeException e) {
	  System.out.println("Error creating block structure.");
	}
  }
}
