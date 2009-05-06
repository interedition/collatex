package com.sd_editions.collatex.permutations;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.List;

import junit.framework.TestCase;

public class WitnessTest extends TestCase {

  private static final String PLAIN_TEXT_A = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse non libero sed augue porttitor blandit nec id nunc. Maecenas sit amet mauris ante.";
  private static final String PLAIN_TEXT_B = "Lorem ipsum dolor sit amit, consetur adipiscing elit suspendisse non libero sed augue blandit nunc. Maeceneas sit amet mauris ante.";
  private static final String XML_SIMPLE_A = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TEI xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns:math=\"http://www.w3.org/1998/Math/MathML\" xmlns=\"http://www.tei-c.org/ns/1.0\">\n  <teiHeader>\n    <fileDesc>\n      <titleStmt>\n        <title></title>\n        <author></author>\n        <respStmt xml:id=\"tla\">\n          <resp>Transcription by</resp>\n          <name> Tara L Andrews</name>\n        </respStmt>\n      </titleStmt>\n      <publicationStmt>\n        <p>Unpublished manuscript</p>\n      </publicationStmt>\n      <sourceDesc>\n        <msDesc xml:id=\"A\">\n          <msIdentifier>\n            <settlement>Bzommar</settlement>\n            <repository>Bibliothek des Klosters</repository>\n            <idno>449</idno>\n          </msIdentifier>\n          <p>pp. 163r-174r</p>\n        </msDesc>\n      </sourceDesc>\n    </fileDesc>\n  </teiHeader>\n  <text>\n    <body>\n      <p><!-- began on page 162r --><pb n=\"163\u0561\"/> Lorem ipsum dolor sit amet, consectetur\n        adipiscing elit. <lb/>Suspendisse non libero sed augue porttitor blandit nec id <lb/>nunc.\n        Maecenas sit amet mauris ante.<lb/>\n      </p>\n    </body>\n  </text>\n</TEI>\n";
  private static final String XML_SIMPLE_B = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TEI xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns:math=\"http://www.w3.org/1998/Math/MathML\" xmlns=\"http://www.tei-c.org/ns/1.0\">\n  <teiHeader>\n    <fileDesc>\n      <titleStmt>\n        <title></title>\n        <author></author>\n        <respStmt xml:id=\"tla\">\n          <resp>Transcription by</resp>\n          <name> Tara L Andrews</name>\n        </respStmt>\n      </titleStmt>\n      <publicationStmt>\n        <p>Unpublished manuscript</p>\n      </publicationStmt>\n      <sourceDesc>\n        <msDesc xml:id=\"B\">\n          <msIdentifier>\n            <settlement>Bzommar</settlement>\n            <repository>Bibliothek des Klosters</repository>\n            <idno>449</idno>\n          </msIdentifier>\n          <p>pp. 163r-174r</p>\n        </msDesc>\n      </sourceDesc>\n    </fileDesc>\n  </teiHeader>\n  <text>\n    <body>\n      <div>\n\t<p>\n\t  <pb n=\"163\"/>\n\tLorem ipsum dolor sit <lb/> \n\t  amit, consetur adipiscing elit <lb/> \n\t  suspendisse non libero sed augue blan<lb/>dit \n\t  nunc. Maeceneas sit amet mauris ante.<lb/>\n\t</p>\n      </div>\n    </body>\n  </text>\n</TEI>\n";
  private static final String XML_TOKEN_A = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TEI xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns:math=\"http://www.w3.org/1998/Math/MathML\" xmlns=\"http://www.tei-c.org/ns/1.0\">\n  <teiHeader>\n    <fileDesc>\n      <titleStmt>\n        <title></title>\n        <author></author>\n        <respStmt xml:id=\"tla\">\n          <resp>Transcription by</resp>\n          <name> Tara L Andrews</name>\n        </respStmt>\n      </titleStmt>\n      <publicationStmt>\n        <p>Unpublished manuscript</p>\n      </publicationStmt>\n      <sourceDesc>\n        <msDesc xml:id=\"A\">\n          <msIdentifier>\n            <settlement>Bzommar</settlement>\n            <repository>Bibliothek des Klosters</repository>\n            <idno>449</idno>\n          </msIdentifier>\n          <p>pp. 163r-174r</p>\n        </msDesc>\n      </sourceDesc>\n    </fileDesc>\n  </teiHeader>\n  <text>\n    <body>\n      <p>\n\t<!-- began on page 162r -->\n\t<pb n=\"163\"/> \n\t<w>Lorem</w>\n\t<w>ipsum</w> \n\t<w>dolor</w> \n\t<w>sit</w> \n\t<w>amet,</w> \n\t<w>consectetur</w>\n        <w>adipiscing</w> \n\t<w>elit.</w> <lb/> \n\t<w>Suspendisse</w> \n\t<w>non</w> \n\t<w>libero</w> \n\t<w>sed</w> \n\t<w>augue</w> \n\t<w>porttitor</w> \n\t<w>blandit</w> \n\t<w>nec</w> \n\t<w>id</w> <lb/> \n\t<w>nunc.</w>\n        <w>Maecenas</w> \n\t<w>sit</w> \n\t<w>amet</w> \n\t<w>mauris</w> \n\t<w>ante.</w><lb/>\n      </p>\n    </body>\n  </text>\n</TEI>\n";
  private static final String XML_TOKEN_B = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TEI xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns:math=\"http://www.w3.org/1998/Math/MathML\" xmlns=\"http://www.tei-c.org/ns/1.0\">\n  <teiHeader>\n    <fileDesc>\n      <titleStmt>\n        <title></title>\n        <author></author>\n        <respStmt xml:id=\"tla\">\n          <resp>Transcription by</resp>\n          <name> Tara L Andrews</name>\n        </respStmt>\n      </titleStmt>\n      <publicationStmt>\n        <p>Unpublished manuscript</p>\n      </publicationStmt>\n      <sourceDesc>\n        <msDesc xml:id=\"B\">\n          <msIdentifier>\n            <settlement>Bzommar</settlement>\n            <repository>Bibliothek des Klosters</repository>\n            <idno>449</idno>\n          </msIdentifier>\n          <p>pp. 163r-174r</p>\n        </msDesc>\n      </sourceDesc>\n    </fileDesc>\n  </teiHeader>\n  <text>\n    <body>\n      <div>\n\t<p>\n\t  <pb n=\"163\"/> \n\t  <w>Lorem</w> \n\t  <w>ipsum</w> \n\t  <w>dolor</w> \n\t  <w>sit</w><lb/> \n\t  <w>amit,</w>\n\t  <w>consetur</w>\n\t  <w>adipiscing</w>\n\t  <w>elit</w><lb/> \n\t  <w>suspendisse</w> \n\t  <w>non</w>\n\t  <w>libero</w>\n\t  <w>sed</w>\n\t  <w>augue</w>\n\t  <w><seg>blan<lb/>dit</seg></w> \n\t  <w>nunc.</w>\n\t  <w>Maeceneas</w> \n\t  <w>sit</w> \n\t  <w>amet</w> \n\t  <w>mauris</w> \n\t  <w>ante.</w><lb/>\n\t</p>\n      </div>\n    </body>\n  </text>\n</TEI>\n";

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

  @Override
  public void setUp() {
    xmlSimpleA = new StringBufferInputStream(XML_SIMPLE_A);
    xmlSimpleB = new StringBufferInputStream(XML_SIMPLE_B);
    xmlTokenA = new StringBufferInputStream(XML_TOKEN_A);
    xmlTokenB = new StringBufferInputStream(XML_TOKEN_B);

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

  private void compateWitnesses(Witness w1, Witness w2) {
    List<Word> words1 = w1.getWords();
    List<Word> words2 = w2.getWords();

    assertEquals("Invalid number of words!", words1.size(), words2.size());

    for (int i = 0; i < words1.size(); i++) {
      assertEquals("Words on the same position are not equal!", words1.get(i).toString(), words2.get(i).toString());
    }
  }

  public void testXmlA() {
    Witness w1 = new Witness(PLAIN_TEXT_A);
    Witness w2 = new Witness(xmlSimpleA);
    Witness w3 = new Witness(xmlTokenA);

    compateWitnesses(w1, w2);
    compateWitnesses(w1, w3);
  }

  public void testXmlB() {
    Witness w1 = new Witness(PLAIN_TEXT_B);
    Witness w2 = new Witness(xmlSimpleB);
    Witness w3 = new Witness(xmlTokenB);

    compateWitnesses(w1, w2);
    compateWitnesses(w1, w3);
  }

  public void testXMLFileA() {
    Witness w1 = new Witness(xmlFileSimpleA);
    Witness w2 = new Witness(xmlFileTokenA);

    compateWitnesses(w1, w2);
  }

  public void testXMLFileB() {
    Witness w1 = new Witness(xmlFileSimpleB);
    Witness w2 = new Witness(xmlFileTokenB);

    compateWitnesses(w1, w2);
  }
}
