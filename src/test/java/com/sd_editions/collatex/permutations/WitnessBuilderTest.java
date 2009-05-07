package com.sd_editions.collatex.permutations;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

public class WitnessBuilderTest extends TestCase {

  private static final String PLAIN_TEXT_A = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse non libero sed augue porttitor blandit nec id nunc. Maecenas sit amet mauris ante.";
  private static final String PLAIN_TEXT_B = "Lorem ipsum dolor sit amit, consetur adipiscing elit suspendisse non libero sed augue blandit nunc. Maeceneas sit amet mauris ante.";
  private static final String XML_SIMPLE_A = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TEI xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns:math=\"http://www.w3.org/1998/Math/MathML\" xmlns=\"http://www.tei-c.org/ns/1.0\">\n  <teiHeader>\n    <fileDesc>\n      <titleStmt>\n        <title></title>\n        <author></author>\n        <respStmt xml:id=\"tla\">\n          <resp>Transcription by</resp>\n          <name> Tara L Andrews</name>\n        </respStmt>\n      </titleStmt>\n      <publicationStmt>\n        <p>Unpublished manuscript</p>\n      </publicationStmt>\n      <sourceDesc>\n        <msDesc xml:id=\"A\">\n          <msIdentifier>\n            <settlement>Bzommar</settlement>\n            <repository>Bibliothek des Klosters</repository>\n            <idno>449</idno>\n          </msIdentifier>\n          <p>pp. 163r-174r</p>\n        </msDesc>\n      </sourceDesc>\n    </fileDesc>\n  </teiHeader>\n  <text>\n    <body>\n      <p><!-- began on page 162r --><pb n=\"163\u0561\"/> Lorem ipsum dolor sit amet, consectetur\n        adipiscing elit. <lb/>Suspendisse non libero sed augue porttitor blandit nec id <lb/>nunc.\n        Maecenas sit amet mauris ante.<lb/>\n      </p>\n    </body>\n  </text>\n</TEI>\n";
  private static final String XML_SIMPLE_B = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TEI xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns:math=\"http://www.w3.org/1998/Math/MathML\" xmlns=\"http://www.tei-c.org/ns/1.0\">\n  <teiHeader>\n    <fileDesc>\n      <titleStmt>\n        <title></title>\n        <author></author>\n        <respStmt xml:id=\"tla\">\n          <resp>Transcription by</resp>\n          <name> Tara L Andrews</name>\n        </respStmt>\n      </titleStmt>\n      <publicationStmt>\n        <p>Unpublished manuscript</p>\n      </publicationStmt>\n      <sourceDesc>\n        <msDesc xml:id=\"B\">\n          <msIdentifier>\n            <settlement>Bzommar</settlement>\n            <repository>Bibliothek des Klosters</repository>\n            <idno>449</idno>\n          </msIdentifier>\n          <p>pp. 163r-174r</p>\n        </msDesc>\n      </sourceDesc>\n    </fileDesc>\n  </teiHeader>\n  <text>\n    <body>\n      <div>\n\t<p>\n\t  <pb n=\"163\"/>\n\tLorem ipsum dolor sit <lb/> \n\t  amit, consetur adipiscing elit <lb/> \n\t  suspendisse non libero sed augue blan<lb/>dit \n\t  nunc. Maeceneas sit amet mauris ante.<lb/>\n\t</p>\n      </div>\n    </body>\n  </text>\n</TEI>\n";
  private static final String XML_TOKEN_A = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TEI xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns:math=\"http://www.w3.org/1998/Math/MathML\" xmlns=\"http://www.tei-c.org/ns/1.0\">\n  <teiHeader>\n    <fileDesc>\n      <titleStmt>\n        <title></title>\n        <author></author>\n        <respStmt xml:id=\"tla\">\n          <resp>Transcription by</resp>\n          <name> Tara L Andrews</name>\n        </respStmt>\n      </titleStmt>\n      <publicationStmt>\n        <p>Unpublished manuscript</p>\n      </publicationStmt>\n      <sourceDesc>\n        <msDesc xml:id=\"A\">\n          <msIdentifier>\n            <settlement>Bzommar</settlement>\n            <repository>Bibliothek des Klosters</repository>\n            <idno>449</idno>\n          </msIdentifier>\n          <p>pp. 163r-174r</p>\n        </msDesc>\n      </sourceDesc>\n    </fileDesc>\n  </teiHeader>\n  <text>\n    <body>\n      <p>\n\t<!-- began on page 162r -->\n\t<pb n=\"163\"/> \n\t<w>Lorem</w>\n\t<w>ipsum</w> \n\t<w>dolor</w> \n\t<w>sit</w> \n\t<w>amet,</w> \n\t<w>consectetur</w>\n        <w>adipiscing</w> \n\t<w>elit.</w> <lb/> \n\t<w>Suspendisse</w> \n\t<w>non</w> \n\t<w>libero</w> \n\t<w>sed</w> \n\t<w>augue</w> \n\t<w>porttitor</w> \n\t<w>blandit</w> \n\t<w>nec</w> \n\t<w>id</w> <lb/> \n\t<w>nunc.</w>\n        <w>Maecenas</w> \n\t<w>sit</w> \n\t<w>amet</w> \n\t<w>mauris</w> \n\t<w>ante.</w><lb/>\n      </p>\n    </body>\n  </text>\n</TEI>\n";
  private static final String XML_TOKEN_B = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TEI xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns:math=\"http://www.w3.org/1998/Math/MathML\" xmlns=\"http://www.tei-c.org/ns/1.0\">\n  <teiHeader>\n    <fileDesc>\n      <titleStmt>\n        <title></title>\n        <author></author>\n        <respStmt xml:id=\"tla\">\n          <resp>Transcription by</resp>\n          <name> Tara L Andrews</name>\n        </respStmt>\n      </titleStmt>\n      <publicationStmt>\n        <p>Unpublished manuscript</p>\n      </publicationStmt>\n      <sourceDesc>\n        <msDesc xml:id=\"B\">\n          <msIdentifier>\n            <settlement>Bzommar</settlement>\n            <repository>Bibliothek des Klosters</repository>\n            <idno>449</idno>\n          </msIdentifier>\n          <p>pp. 163r-174r</p>\n        </msDesc>\n      </sourceDesc>\n    </fileDesc>\n  </teiHeader>\n  <text>\n    <body>\n      <div>\n\t<p>\n\t  <pb n=\"163\"/> \n\t  <w>Lorem</w> \n\t  <w>ipsum</w> \n\t  <w>dolor</w> \n\t  <w>sit</w><lb/> \n\t  <w>amit,</w>\n\t  <w>consetur</w>\n\t  <w>adipiscing</w>\n\t  <w>elit</w><lb/> \n\t  <w>suspendisse</w> \n\t  <w>non</w>\n\t  <w>libero</w>\n\t  <w>sed</w>\n\t  <w>augue</w>\n\t  <w><seg>blan<lb/>dit</seg></w> \n\t  <w>nunc.</w>\n\t  <w>Maeceneas</w> \n\t  <w>sit</w> \n\t  <w>amet</w> \n\t  <w>mauris</w> \n\t  <w>ante.</w><lb/>\n\t</p>\n      </div>\n    </body>\n  </text>\n</TEI>\n";
  private static final String EMPTY_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TEI xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns=\"http://www.tei-c.org/ns/1.0\">\n  <teiHeader></teiHeader><text><body></body></text></TEI>";
  private InputStream xmlSimpleA;
  private InputStream xmlSimpleB;
  private InputStream xmlTokenA;
  private InputStream xmlTokenB;
  private FileInputStream plainTextFileA;
  private FileInputStream plainTextFileB;
  private FileInputStream xmlFileSimpleA;
  private FileInputStream xmlFileTokenB;
  private FileInputStream xmlFileTokenA;
  private FileInputStream xmlFileSimpleB;
  private InputStream brokenXmlSimpleA;

  private WitnessBuilder witnessBuilder;
  private ByteArrayInputStream emptyXml;

  @Override
  public void setUp() {
    try {
      xmlSimpleA = new ByteArrayInputStream(XML_SIMPLE_A.getBytes("utf-8"));
      xmlSimpleB = new ByteArrayInputStream(XML_SIMPLE_B.getBytes("utf-8"));
      xmlTokenA = new ByteArrayInputStream(XML_TOKEN_A.getBytes("utf-8"));
      xmlTokenB = new ByteArrayInputStream(XML_TOKEN_B.getBytes("utf-8"));
      brokenXmlSimpleA = new ByteArrayInputStream(XML_SIMPLE_A.substring(0, XML_SIMPLE_A.length() - 20).getBytes("utf-8"));
      emptyXml = new ByteArrayInputStream(EMPTY_XML.getBytes("utf-8"));
    } catch (UnsupportedEncodingException e1) {
      //
    }

    try {
      plainTextFileA = new FileInputStream("examples/testfiles/plaintext_a.txt");
      plainTextFileB = new FileInputStream("examples/testfiles/plaintext_b.txt");

      xmlFileSimpleA = new FileInputStream("examples/testfiles/xml_simple_a.xml");
      xmlFileSimpleB = new FileInputStream("examples/testfiles/xml_simple_b.xml");

      xmlFileTokenA = new FileInputStream("examples/testfiles/xml_token_a.xml");
      xmlFileTokenB = new FileInputStream("examples/testfiles/xml_token_b.xml");

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    witnessBuilder = new WitnessBuilder();
  }

  @Override
  public void tearDown() throws Exception {
    plainTextFileA.close();
    plainTextFileB.close();
    xmlFileSimpleA.close();
    xmlFileSimpleB.close();
    xmlFileTokenA.close();
    xmlFileTokenB.close();
  }

  private void compareWitnesses(Witness w1, Witness w2) {
    List<Word> words1 = w1.getWords();
    List<Word> words2 = w2.getWords();

    assertEquals("Invalid number of words!", words1.size(), words2.size());

    for (int i = 0; i < words1.size(); i++) {
      assertEquals("Words on the same position are not equal!", words1.get(i).toString(), words2.get(i).toString());
    }
  }

  public void testXmlA() throws SAXException, IOException {
    Witness w1 = witnessBuilder.build(PLAIN_TEXT_A);
    Witness w2 = null;
    w2 = witnessBuilder.build(xmlSimpleA);
    Witness w3 = null;
    w3 = witnessBuilder.build(xmlTokenA);
    compareWitnesses(w1, w2);
    compareWitnesses(w1, w3);
  }

  public void testXmlB() throws SAXException, IOException {
    Witness w1 = witnessBuilder.build(PLAIN_TEXT_B);
    Witness w2 = witnessBuilder.build(xmlSimpleB);
    Witness w3 = witnessBuilder.build(xmlTokenB);

    compareWitnesses(w1, w2);
    compareWitnesses(w1, w3);
  }

  public void testXMLFileA() throws SAXException, IOException {
    Witness w1 = witnessBuilder.build(xmlFileSimpleA);
    Witness w2 = witnessBuilder.build(xmlFileTokenA);

    compareWitnesses(w1, w2);
  }

  public void testXMLFileB() throws SAXException, IOException {
    Witness w1 = witnessBuilder.build(xmlFileSimpleB);
    Witness w2 = witnessBuilder.build(xmlFileTokenB);

    compareWitnesses(w1, w2);
  }

  public void testEmptyStream() {
    Witness w = null;
    try {
      w = witnessBuilder.build(new ByteArrayInputStream(new byte[0]));
      fail();
    } catch (SAXException e) {
      //
    } catch (IOException e) {
      fail();
    }
    assertEquals(w, null);
  }

  public void testNullStream() {
    try {
      try {
        Witness w = witnessBuilder.build((InputStream) null);
      } catch (SAXException e) {
        fail();
      } catch (IOException e) {
        fail();
      }
      fail();
    } catch (IllegalArgumentException e) {
      //
    }
  }

  public void testBrokenXml() {
    Witness brokenW = null;
    try {
      brokenW = witnessBuilder.build(brokenXmlSimpleA);
      fail();
    } catch (SAXException e) {
      //
    } catch (IOException e) {
      fail();
    }
  }

  public void testEmptyXml() {
    try {
      Witness w = witnessBuilder.build(emptyXml);
    } catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void testEmptyWitnessFromString() {
    Witness witness = witnessBuilder.build("");
    assertEquals(witness.sentence, "");
    assertEquals(witness.getWords().size(), 0);
  }

}
