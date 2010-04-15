package com.sd_editions.collatex.InputPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;
import com.sd_editions.collatex.Block.Line;
import com.sd_editions.collatex.Block.Word;

public class XMLInputPlugin implements IntInputPlugin {
  private final File xmlFile;

  public XMLInputPlugin(File xmlFile1) {
    this.xmlFile = xmlFile1;
  }

  public BlockStructure readFile() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    try {
      SAXHandler saxHandler = new SAXHandler();
      SAXParserFactory.newInstance().newSAXParser().parse(new InputSource(new FileReader(xmlFile)), saxHandler);
      BlockStructure document = saxHandler.getDocument();
      return document;
    } catch (SAXException e) {
      throw new RuntimeException(e);
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  public void registerInputPlugin() {
    // Do nothing for the moment
  }

  public static class SAXHandler extends DefaultHandler {

    private final BlockStructure document;
    private int lineCount;
    private Line pLine;
    private StringBuffer text;

    public SAXHandler() {
      document = new BlockStructure();
      text = new StringBuffer();
    }

    public BlockStructure getDocument() {
      return document;
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
      if (name.equals("l")) {
        lineCount++;
        Line nLine = new Line(lineCount);
        if (pLine == null) {
          try {
            document.setRootBlock(nLine, true);
          } catch (BlockStructureCascadeException e) {
            throw new RuntimeException(e);
          }
        } else {
          document.setNextSibling(pLine, nLine);
        }
        pLine = nLine;
      } else if (name.equals("w")) {
        text = new StringBuffer();
      }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      text.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
      if (name.equals("w")) {
        Word word = new Word(text.toString());
        document.setChildBlock(pLine, word);
      }
    }
  }

}
