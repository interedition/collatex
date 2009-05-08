package com.sd_editions.collatex.permutations;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

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

import com.google.common.collect.Lists;

public class WitnessXmlBuilder extends WitnessStreamBuilder {

  DocumentBuilder builder = null;

  XPathFactory factory;

  public WitnessXmlBuilder() {
    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    try {
      builder = domFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new InternalError("Internal error. Can't create document builder!");
    }
    factory = XPathFactory.newInstance();
  }

  @Override
  public Witness build(InputStream inputStream) throws SAXException, IOException {
    String id = Long.toString(Math.abs(new Random().nextLong()), 5);
    Document doc = getXmlDocument(inputStream);

    XPath xpath = factory.newXPath();
    Object result;
    result = evaluate(xpath, doc, "/TEI/text/body//w");
    NodeList nodes = (NodeList) result;
    List<Word> words = Lists.newArrayList();
    if (nodes == null || nodes.getLength() == 0) {//get text from 'p' elements
      result = evaluate(xpath, doc, "/TEI/text/body//p");
      nodes = (NodeList) result;
      int counter = 1;
      StringBuilder builder1 = new StringBuilder();
      for (int i = 0; i < nodes.getLength(); i++) {
        String value = nodes.item(i).getTextContent();
        builder1.append(value);
      }
      Witness w = build(builder1.toString());
      for (Word word : w.getWords()) {
        words.add(new Word(id, word.toString(), counter++));
      }
    } else { // get text from prepared 'w' elements 
      int counter = 1;
      for (int i = 0; i < nodes.getLength(); i++) {
        String value = nodes.item(i).getTextContent();
        words.add(new Word(id, value, counter++));
      }
    }
    return new Witness(words.toArray(new Word[0]));
  }

  private Object evaluate(XPath xpath, Document doc, String exprression) {
    XPathExpression expr;
    try {
      expr = xpath.compile(exprression);
    } catch (XPathExpressionException e) {
      throw new InternalError("Incorrect xpath expression!");
    }

    Object result;
    try {
      result = expr.evaluate(doc, XPathConstants.NODESET);
    } catch (XPathExpressionException e) {
      throw new InternalError("Incorrect xpath expression!");
    }
    return result;
  }

  private Document getXmlDocument(InputStream xmlInputStream) throws SAXException, IOException {
    Document doc = null;
    //    domFactory.setNamespaceAware(true);
    doc = builder.parse(xmlInputStream);
    return doc;
  }

}
