/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex2.input.builders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import eu.interedition.collatex2.implementation.tokenization.DefaultTokenNormalizer;
import eu.interedition.collatex2.input.builders.WitnessBuilder.ContentType;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class WitnessBuilderTest {
  private static final String PLAIN_TEXT_A = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse non libero sed augue porttitor blandit nec id nunc. Maecenas sit amet mauris ante.";
  private static final String PLAIN_TEXT_B = "Lorem ipsum dolor sit amit, consetur adipiscing elit suspendisse non libero sed augue blandit nunc. Maeceneas sit amet mauris ante.";
  private static final String XML_SIMPLE_A = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TEI xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns:math=\"http://www.w3.org/1998/Math/MathML\" xmlns=\"http://www.tei-c.org/ns/1.0\">\n  <teiHeader>\n    <fileDesc>\n      <titleStmt>\n        <title></title>\n        <author></author>\n        <respStmt xml:id=\"tla\">\n          <resp>Transcription by</resp>\n          <name> Tara L Andrews</name>\n        </respStmt>\n      </titleStmt>\n      <publicationStmt>\n        <p>Unpublished manuscript</p>\n      </publicationStmt>\n      <sourceDesc>\n        <msDesc xml:id=\"A\">\n          <msIdentifier>\n            <settlement>Bzommar</settlement>\n            <repository>Bibliothek des Klosters</repository>\n            <idno>449</idno>\n          </msIdentifier>\n          <p>pp. 163r-174r</p>\n        </msDesc>\n      </sourceDesc>\n    </fileDesc>\n  </teiHeader>\n  <text>\n    <body>\n      <p><!-- began on page 162r --><pb n=\"163\u0561\"/> Lorem ipsum dolor sit amet, consectetur\n        adipiscing elit. <lb/>Suspendisse non libero sed augue porttitor blandit nec id <lb/>nunc.\n        Maecenas sit amet mauris ante.<lb/>\n      </p>\n    </body>\n  </text>\n</TEI>\n";
  private static final String XML_SIMPLE_B = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TEI xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns:math=\"http://www.w3.org/1998/Math/MathML\" xmlns=\"http://www.tei-c.org/ns/1.0\">\n  <teiHeader>\n    <fileDesc>\n      <titleStmt>\n        <title></title>\n        <author></author>\n        <respStmt xml:id=\"tla\">\n          <resp>Transcription by</resp>\n          <name> Tara L Andrews</name>\n        </respStmt>\n      </titleStmt>\n      <publicationStmt>\n        <p>Unpublished manuscript</p>\n      </publicationStmt>\n      <sourceDesc>\n        <msDesc xml:id=\"B\">\n          <msIdentifier>\n            <settlement>Bzommar</settlement>\n            <repository>Bibliothek des Klosters</repository>\n            <idno>449</idno>\n          </msIdentifier>\n          <p>pp. 163r-174r</p>\n        </msDesc>\n      </sourceDesc>\n    </fileDesc>\n  </teiHeader>\n  <text>\n    <body>\n      <div>\n\t<p>\n\t  <pb n=\"163\"/>\n\tLorem ipsum dolor sit <lb/> \n\t  amit, consetur adipiscing elit <lb/> \n\t  suspendisse non libero sed augue blan<lb/>dit \n\t  nunc. Maeceneas sit amet mauris ante.<lb/>\n\t</p>\n      </div>\n    </body>\n  </text>\n</TEI>\n";
  private static final String XML_TOKEN_A = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TEI xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns:math=\"http://www.w3.org/1998/Math/MathML\" xmlns=\"http://www.tei-c.org/ns/1.0\">\n  <teiHeader>\n    <fileDesc>\n      <titleStmt>\n        <title></title>\n        <author></author>\n        <respStmt xml:id=\"tla\">\n          <resp>Transcription by</resp>\n          <name> Tara L Andrews</name>\n        </respStmt>\n      </titleStmt>\n      <publicationStmt>\n        <p>Unpublished manuscript</p>\n      </publicationStmt>\n      <sourceDesc>\n        <msDesc xml:id=\"A\">\n          <msIdentifier>\n            <settlement>Bzommar</settlement>\n            <repository>Bibliothek des Klosters</repository>\n            <idno>449</idno>\n          </msIdentifier>\n          <p>pp. 163r-174r</p>\n        </msDesc>\n      </sourceDesc>\n    </fileDesc>\n  </teiHeader>\n  <text>\n    <body>\n      <p>\n\t<!-- began on page 162r -->\n\t<pb n=\"163\"/> \n\t<w>Lorem</w>\n\t<w>ipsum</w> \n\t<w>dolor</w> \n\t<w>sit</w> \n\t<w>amet,</w> \n\t<w>consectetur</w>\n        <w>adipiscing</w> \n\t<w>elit.</w> <lb/> \n\t<w>Suspendisse</w> \n\t<w>non</w> \n\t<w>libero</w> \n\t<w>sed</w> \n\t<w>augue</w> \n\t<w>porttitor</w> \n\t<w>blandit</w> \n\t<w>nec</w> \n\t<w>id</w> <lb/> \n\t<w>nunc.</w>\n        <w>Maecenas</w> \n\t<w>sit</w> \n\t<w>amet</w> \n\t<w>mauris</w> \n\t<w>ante.</w><lb/>\n      </p>\n    </body>\n  </text>\n</TEI>\n";
  private static final String XML_TOKEN_B = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TEI xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns:math=\"http://www.w3.org/1998/Math/MathML\" xmlns=\"http://www.tei-c.org/ns/1.0\">\n  <teiHeader>\n    <fileDesc>\n      <titleStmt>\n        <title></title>\n        <author></author>\n        <respStmt xml:id=\"tla\">\n          <resp>Transcription by</resp>\n          <name> Tara L Andrews</name>\n        </respStmt>\n      </titleStmt>\n      <publicationStmt>\n        <p>Unpublished manuscript</p>\n      </publicationStmt>\n      <sourceDesc>\n        <msDesc xml:id=\"B\">\n          <msIdentifier>\n            <settlement>Bzommar</settlement>\n            <repository>Bibliothek des Klosters</repository>\n            <idno>449</idno>\n          </msIdentifier>\n          <p>pp. 163r-174r</p>\n        </msDesc>\n      </sourceDesc>\n    </fileDesc>\n  </teiHeader>\n  <text>\n    <body>\n      <div>\n\t<p>\n\t  <pb n=\"163\"/> \n\t  <w>Lorem</w> \n\t  <w>ipsum</w> \n\t  <w>dolor</w> \n\t  <w>sit</w><lb/> \n\t  <w>amit,</w>\n\t  <w>consetur</w>\n\t  <w>adipiscing</w>\n\t  <w>elit</w><lb/> \n\t  <w>suspendisse</w> \n\t  <w>non</w>\n\t  <w>libero</w>\n\t  <w>sed</w>\n\t  <w>augue</w>\n\t  <w><seg>blan<lb/>dit</seg></w> \n\t  <w>nunc.</w>\n\t  <w>Maeceneas</w> \n\t  <w>sit</w> \n\t  <w>amet</w> \n\t  <w>mauris</w> \n\t  <w>ante.</w><lb/>\n\t</p>\n      </div>\n    </body>\n  </text>\n</TEI>\n";
  private static final String EMPTY_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TEI xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns=\"http://www.tei-c.org/ns/1.0\">\n  <teiHeader></teiHeader><text><body></body></text></TEI>";
  private static InputStream xmlSimpleA;
  private static InputStream xmlSimpleB;
  private static InputStream xmlTokenA;
  private static InputStream xmlTokenB;
  private static FileInputStream plainTextFileA;
  private static FileInputStream plainTextFileB;
  private static FileInputStream xmlFileSimpleA;
  private static FileInputStream xmlFileTokenB;
  private static FileInputStream xmlFileTokenA;
  private static FileInputStream xmlFileSimpleB;
  private static InputStream brokenXmlSimpleA;
  private static WitnessBuilder witnessBuilder;
  private static ByteArrayInputStream emptyXml;

  @BeforeClass
  public static void setUp() {
    System.out.println("start setUp()");
    try {
      xmlSimpleA = new ByteArrayInputStream(XML_SIMPLE_A.getBytes("utf-8"));
      xmlSimpleB = new ByteArrayInputStream(XML_SIMPLE_B.getBytes("utf-8"));
      xmlTokenA = new ByteArrayInputStream(XML_TOKEN_A.getBytes("utf-8"));
      xmlTokenB = new ByteArrayInputStream(XML_TOKEN_B.getBytes("utf-8"));
      brokenXmlSimpleA = new ByteArrayInputStream(XML_SIMPLE_A.substring(0, XML_SIMPLE_A.length() - 20).getBytes("utf-8"));
      emptyXml = new ByteArrayInputStream(EMPTY_XML.getBytes("utf-8"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    try {
      plainTextFileA = new FileInputStream("examples/testfiles/plaintext_a.txt");
      plainTextFileB = new FileInputStream("examples/testfiles/plaintext_b.txt");

      xmlFileSimpleA = new FileInputStream("examples/testfiles/xml_simple_a.xml");
      xmlFileSimpleB = new FileInputStream("examples/testfiles/xml_simple_b.xml");

      xmlFileTokenA = new FileInputStream("examples/testfiles/xml_token_a.xml");
      xmlFileTokenB = new FileInputStream("examples/testfiles/xml_token_b.xml");
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    witnessBuilder = new WitnessBuilder(new DefaultTokenNormalizer());
    System.out.println("end setUp()");
  }

  @AfterClass
  public static void tearDown() throws Exception {
    plainTextFileA.close();
    plainTextFileB.close();
    xmlFileSimpleA.close();
    xmlFileSimpleB.close();
    xmlFileTokenA.close();
    xmlFileTokenB.close();
  }

  private void compareWitnesses(IWitness w1, IWitness w2) {
    List<INormalizedToken> w1tokens = w1.getTokens();
    List<INormalizedToken> w2tokens = w2.getTokens();
    assertEquals("Invalid number of words!", w1tokens.size(), w2tokens.size());
    for (int i = 0; i < w1tokens.size(); i++) {
      assertEquals("Words on the same position are not equal!", w1tokens.get(i).getContent(), w2tokens.get(i).getContent());
    }
  }

  @Test
  public void testXmlA() throws SAXException, IOException {
    IWitness w1 = witnessBuilder.build(PLAIN_TEXT_A);
    IWitness w2 = null;
    w2 = witnessBuilder.build(xmlSimpleA, ContentType.TEXT_XML);
    IWitness w3 = null;
    w3 = witnessBuilder.build(xmlTokenA, ContentType.TEXT_XML);
    compareWitnesses(w1, w2);
    compareWitnesses(w1, w3);
  }

  @Test
  public void testXmlB() throws SAXException, IOException {
    IWitness w1 = witnessBuilder.build(PLAIN_TEXT_B);
    IWitness w2 = witnessBuilder.build(xmlSimpleB, ContentType.TEXT_XML);
    IWitness w3 = witnessBuilder.build(xmlTokenB, ContentType.TEXT_XML);

    compareWitnesses(w1, w2);
    compareWitnesses(w1, w3);
  }

  @Ignore
  @Test
  public void testXMLFileA() throws SAXException, IOException {
    IWitness w1 = witnessBuilder.build(xmlFileSimpleA, ContentType.TEXT_XML);
    IWitness w2 = witnessBuilder.build(xmlFileTokenA, ContentType.TEXT_XML);
    IWitness w3 = witnessBuilder.build(plainTextFileA, ContentType.TEXT_PLAIN);

    compareWitnesses(w1, w2);
    compareWitnesses(w1, w3);
    compareWitnesses(w2, w3);
  }

  @Ignore
  @Test
  public void testXMLFileB() throws SAXException, IOException {
    IWitness w1 = witnessBuilder.build(xmlFileSimpleB, ContentType.TEXT_XML);
    IWitness w2 = witnessBuilder.build(xmlFileTokenB, ContentType.TEXT_XML);
    IWitness w3 = witnessBuilder.build(plainTextFileB, ContentType.TEXT_PLAIN);

    compareWitnesses(w1, w2);
    compareWitnesses(w1, w3);
    compareWitnesses(w2, w3);
  }

  @Test
  public void testEmptyStream() {
    IWitness w = null;
    try {
      w = witnessBuilder.build(new ByteArrayInputStream(new byte[0]), ContentType.TEXT_XML);
      fail();
    } catch (SAXException e) {
      //
    } catch (IOException e) {
      fail();
    }
    assertEquals(w, null);
  }

  @Test
  public void testNullStream() {
    try {
      try {
        witnessBuilder.build((InputStream) null, ContentType.TEXT_XML);
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
    try {
      witnessBuilder.build(brokenXmlSimpleA, ContentType.TEXT_XML);
      fail();
    } catch (SAXException e) {
      //
    } catch (IOException e) {
      fail();
    }
  }

  @Test
  public void testEmptyXml() {
    try {
      IWitness w = witnessBuilder.build(emptyXml, ContentType.TEXT_XML);
      assertEquals(w.getTokens().size(), 0);
    } catch (SAXException e) {
      fail();
    } catch (IOException e) {
      fail();
    }
  }

  @Test
  public void testEmptyWitnessFromString() {
    IWitness witness = witnessBuilder.build("");
    assertEquals(witness.getTokens().size(), 0);
  }

  @Test
  public void testValueOfContentType() {
    assertEquals(ContentType.value("text/xml"), ContentType.TEXT_XML);
    assertEquals(ContentType.value("text/plain"), ContentType.TEXT_PLAIN);
    assertEquals(ContentType.value(""), null);
  }

  @Test
  public void testWrongContentType() {
    try {
      witnessBuilder.build(emptyXml, ContentType.value("xxx"));
      fail();
    } catch (SAXException e) {
      fail();
    } catch (IOException e) {
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals(e.getMessage(), "Given content type is unsupported!");
    }
  }
}
