package com.sd_editions.collatex.permutations;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Join;
import com.google.common.collect.Lists;

public class Witness {
  public final String sentence;
  private final List<Word> words;

  public Witness(String witness) {
    this.sentence = witness;
    WitnessTokenizer tokenizer = new WitnessTokenizer(witness, false);
    this.words = Lists.newArrayList();
    int position = 1;
    while (tokenizer.hasNextToken()) {
      this.words.add(new Word(tokenizer.nextToken(), position));
      position++;
    }
  }

  public Witness(Word... _words) {
    this.sentence = Join.join(" ", _words);
    this.words = Lists.newArrayList(_words);
  }

  public Witness(InputStream xmlInputStream) {
    this.words = Lists.newArrayList();
    Document doc = null;
    doc = getXmlDocument(xmlInputStream);
    if (doc == null) {
      this.sentence = null;
      return;
    }
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();
    Object result;
    try {
      result = evaluate(xpath, doc, "/TEI/text/body//w");
    } catch (Exception e) {
      System.err.println(e);
      sentence = null;
      return;
    }
    NodeList nodes = (NodeList) result;
    if (nodes == null || nodes.getLength() == 0) {//get text from 'p' elements
      try {
        result = evaluate(xpath, doc, "/TEI/text/body//p");
      } catch (XPathExpressionException e) {
        System.err.println(e);
        sentence = null;
        return;
      }
      nodes = (NodeList) result;
      int counter = 0;
      StringBuilder builder1 = new StringBuilder();
      for (int i = 0; i < nodes.getLength(); i++) {
        String value = nodes.item(i).getTextContent();
        builder1.append(value);
      }
      Witness w = new Witness(builder1.toString());
      for (Word word : w.getWords()) {
        words.add(new Word(word.toString(), counter++));
      }
    } else { // get text from prepared 'w' elements 
      int counter = 0;
      for (int i = 0; i < nodes.getLength(); i++) {
        String value = nodes.item(i).getTextContent();
        words.add(new Word(value, counter++));
      }
    }
    sentence = Join.join(" ", words);
  }

  private Object evaluate(XPath xpath, Document doc, String exprression) throws XPathExpressionException {
    XPathExpression expr;
    expr = xpath.compile(exprression);

    Object result;
    result = expr.evaluate(doc, XPathConstants.NODESET);
    return result;
  }

  private Document getXmlDocument(InputStream xmlInputStream) {
    Document doc = null;
    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    //    domFactory.setNamespaceAware(true);
    DocumentBuilder builder = null;
    try {
      builder = domFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      System.err.println("Internal error. Can't create document builder!");
      return null;
    }
    try {
      doc = builder.parse(xmlInputStream);
    } catch (SAXException e1) {
      System.err.println("Can't parse xml stream");
    } catch (IOException e1) {
      System.err.println("Can't parse xml stream");
    }
    return doc;
  }

  public List<Word> getWords() {
    return words;
  }

  public Word getWordOnPosition(int position) {
    return words.get(position - 1);
  }

  public int size() {
    return words.size();
  }
}
