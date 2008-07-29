package com.sd_editions.collatex.InputPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;
import com.sd_editions.collatex.Block.Line;
import com.sd_editions.collatex.Block.Word;

public abstract class AbstractPlainTextInputPlugin implements IntInputPlugin {

  public BlockStructure readFile() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    Reader reader = getReader();
    StreamTokenizer st = new StreamTokenizer(reader);
    st.eolIsSignificant(true);

    BlockStructure document = new BlockStructure();
    int token = st.nextToken();
    // Assume we start on line 1
    int lineCount = 1;
    Line pLine = new Line(lineCount);
    document.setRootBlock(pLine, true);
    while (token != StreamTokenizer.TT_EOF) {
      switch (token) {
      case StreamTokenizer.TT_EOL:
        // Check we aren't actually at the end of the file
        if (st.nextToken() == StreamTokenizer.TT_EOF) {
          st.pushBack();
          break;
        }
        // Found an end of line
        lineCount++;
        Line nLine = new Line(lineCount);
        document.setNextSibling(pLine, nLine);
        pLine = nLine;
        break;
      case StreamTokenizer.TT_WORD:
        // Found a word, add it to our block
        Word word = new Word(st.sval);
        // This adds the word to the last sibling of the line.
        document.setChildBlock(pLine, word);
        break;
      }
      token = st.nextToken();
    }
    // Close the file
    reader.close();
    return document;
  }

  protected abstract Reader getReader() throws FileNotFoundException;

  public void registerInputPlugin() {
  // Do nothing
  }
}
