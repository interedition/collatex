package eu.interedition.collatex.matrixlinker;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.matching.StrictEqualityTokenComparator;
import eu.interedition.collatex.matrixlinker.MatchMatrix.Coordinate;
import eu.interedition.collatex.matrixlinker.MatchMatrix.Island;
import eu.interedition.collatex.simple.SimpleVariantGraphSerializer;
import eu.interedition.collatex.simple.SimpleWitness;

public class HermansTest extends AbstractTest {
  @Before
  public void setUp() {
    setCollationAlgorithm(CollationAlgorithmFactory.dekkerMatchMatrix(new StrictEqualityTokenComparator()));
  }

  @Test
  public void testHermansText1() {
    String textD1 = "Op den Atlantischen Oceaan voer een groote stoomer, de lucht was helder blauw, het water rimpelend satijn.";
    String textD9 = "Over de Atlantische Oceaan voer een grote stomer. De lucht was helder blauw, het water rimpelend satijn.<p/>";
    SimpleWitness[] sw = createWitnesses(textD1, textD9);
    VariantGraph vg = collate(sw[0]);
    MatchMatrix buildMatrix = MatchMatrix.create(vg, sw[1], new EqualityTokenComparator());
    System.out.println(buildMatrix.toHtml());
  }

  @Test
  public void testHermansText2() {
    String textD1 = "Op den Atlantischen Oceaan voer een groote stoomer. Onder de velen aan boojrd bevond zich een bruine, korte dikke man. <i> JSg </i> werd nooit zonder sigaar gezien. Zijn pantalon had lijnrechte vouwen in de pijpen, maar zat toch altijd vol rimpels. <b> De </b> pantalon werd naar boven toe breed, ontzaggelijk breed; hij omsloot den buik van den kleinen man als een soort balcon.";
    String textD9 = "Op de Atlantische Oceaan voer een ontzaggelijk zeekasteel. Onder de vele passagiers aan boord, bevond zich een bruine, korte dikke man. Hij werd nooit zonder sigaar gezien. Zijn pantalon had lijnrechte vouwen in de pijpen, maar zat toch altijd vol rimpels. De pantalon werd naar boven toe breed, ongelofelijk breed: hij omsloot de buik van de kleine man als een soort balkon.";
    SimpleWitness[] sw = createWitnesses(textD1, textD9);
    VariantGraph vg = collate(sw[0]);
    MatchMatrix buildMatrix = MatchMatrix.create(vg, sw[1], new EqualityTokenComparator());
    // System.out.println(buildMatrix.toHtml());
    ArchipelagoWithVersions archipelago = new ArchipelagoWithVersions();
    for (MatchMatrix.Island isl : buildMatrix.getIslands()) {
      archipelago.add(isl);
    }
    System.out.println("archipelago: " + archipelago);
    System.out.println("archipelago.size(): " + archipelago.size());
    assertEquals(42, archipelago.size());
    assertEquals(98, archipelago.numOfConflicts());
    // assertTrue(false);
    // archipelago.createNonConflictingVersions();
    // assertEquals(603,archipelago.numOfNonConflConstell());
    // assertEquals(500,archipelago.getVersion(0).value());
    // assertEquals(497,archipelago.getVersion(4).value());

    Archipelago firstVersion = archipelago.createNonConflictingVersion();
    for (MatchMatrix.Island isl : firstVersion.iterator()) {
      System.out.print(" " + isl.size());
    }
    try {
      int i = 0;
      String file_name = "result_3_" + i + ".html";
      File logFile = new File(File.separator + "C:\\Documents and Settings\\meindert\\Mijn Documenten\\Project Hermans productielijn\\Materiaal input collateX\\output_collatex_exp\\" + file_name);
      PrintWriter logging = new PrintWriter(new FileOutputStream(logFile));
      // logging.println(buildMatrix.toHtml(archipelago.getVersion(i)));
      logging.println(buildMatrix.toHtml(firstVersion));
      logging.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    // for(int i=0; i<10; i++) {
    // try {
    // String file_name = "result_"+i+".html";
    // File logFile = new File(File.separator +
    // "C:\\Documents and Settings\\meindert\\Mijn Documenten\\Project Hermans productielijn\\Materiaal input collateX\\output_collatex_exp\\"+file_name);
    // PrintWriter logging = new PrintWriter(new FileOutputStream(logFile));
    // logging.println(buildMatrix.toHtml(archipelago.getVersion(i)));
    // logging.close();
    // } catch (FileNotFoundException e) {
    // e.printStackTrace();
    // }
    // }

  }

  @Test
  public void testHermansText3() {
    String textMZ_DJ233 = "Werumeus Buning maakt artikelen van vijf pagina&APO+s over de geologie van de diepzee, die hij uit Engelse boeken overschrijft, wat hij pas in de laatste regel vermeldt, omdat hij zo goed kan koken.<p/>\n" + "J. W. Hofstra kan niet lezen en nauwelijks stotteren, laat staan schrijven. Hij oefent het ambt van litterair criticus uit omdat hij uiterlijk veel weg heeft van een Duitse filmacteur (Adolf Wohlbrock).<p/>\n" + "Zo nu en dan koopt Elsevier een artikel van een echte professor wiens naam en titels zu vet worden afgedrukt, dat zij allicht de andere copie ook iets professoraals geven, in het oog van de speksnijders.<p/>\n" + "Edouard Bouquin is het olijke culturele geweten. Bouquin betekent: 1) oud boek van geringe waarde, 2) oude bok, 3) mannetjeskonijn. Ik kan het ook niet helpen, het staat in Larousse.<p/>\n" + "De politiek van dit blad wordt geschreven door een der leeuwen uit het Nederlandse wapen (ik geloof de rechtse) op een krakerige gerechtszaaltoon in zeer korte zinnetjes, omdat hij tot zijn spijt de syntaxis onvoldoende beheerst.<p/>\n";
    String textD4F = "Werumeus  Buning maakt artikelen van vijf pagina&APO+s  over de  geologie van de  diepzee, die  hij uit Engelse  boeken overschrijft,   wat hij  pas in de laatste  regel  vermeldt,   omdat hij   zo  goed kan koken.<p/>\n" + "J. W.Hofstra kan niet lezen en nauwelijks stotteren,   laat staan schrijven.   Hij  oefent het ambt van literair kritikus uit omdat hij uiterlijk veel weg heeft van een Duitse filmacteur (Adolf Wohlbrock).<p/>\n" + "Edouard  Bouquin is  het olijke  culturele  geweten.   Bouquin betekent:   1)  oud boek  van geringe  waarde,   2)  oude bok,   3)  mannetjeskonijn.   Ik kan het ook niet helpen,   het staat in Larousse.<p/>\n" + "Nu en dan koopt Elsevier een artikel van een echte professor, wiens naam en titels zu vet worden afgedrukt, dat zij allicht de andere copie ook iets professoraals geven, in het oog van de speksnijders.<p/>\n" + "\n" + "De politiek van dit blad  wordt geschreven door een der leeuwen uit het nederlandse wapen (ik geloof de   rechtse)  op een krakerige  gerechtszaaltoon in zeer korte  zinnetjes, omdat hij  tot zijn  spijt  de  syntaxis  onvoldoende  beheerst. <p/>";
    SimpleWitness[] sw = createWitnesses(textMZ_DJ233, textD4F);
    VariantGraph vg = collate(sw[0]);
    MatchMatrix buildMatrix = MatchMatrix.create(vg, sw[1], new EqualityTokenComparator());
    // try {
    // FileWriter fw = new
    // FileWriter("C:\\Documents and Settings\\meindert\\Mijn Documenten\\Project Hermans productielijn\\Materiaal input collateX\\Hulp1.html");
    // fw.write(buildMatrix.toHtml());
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // System.out.println(buildMatrix.toHtml());
    ArchipelagoWithVersions archipelago = new ArchipelagoWithVersions();
    for (MatchMatrix.Island isl : buildMatrix.getIslands()) {
      archipelago.add(isl);
    }
    System.out.println("archipelago: " + archipelago);
    System.out.println("archipelago.size(): " + archipelago.size());
    for (MatchMatrix.Island isl : archipelago.iterator()) {
      System.out.print(" " + isl.size());
    }
    System.out.println();
    assertEquals(233, archipelago.size());
    assertEquals(1429, archipelago.numOfConflicts());
    Archipelago firstVersion = archipelago.createNonConflictingVersion();
    for (MatchMatrix.Island isl : firstVersion.iterator()) {
      System.out.print(" " + isl.size());
    }
    try {
      int i = 0;
      String file_name = "result_2_" + i + ".html";
      File logFile = new File(File.separator + "C:\\Documents and Settings\\meindert\\Mijn Documenten\\Project Hermans productielijn\\Materiaal input collateX\\output_collatex_exp\\" + file_name);
      PrintWriter logging = new PrintWriter(new FileOutputStream(logFile));
      // logging.println(buildMatrix.toHtml(archipelago.getVersion(i)));
      logging.println(buildMatrix.toHtml(firstVersion));
      logging.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    //    assertEquals(4877, firstVersion.value());
    // assertTrue(false);

    // archipelago.createNonConflictingVersions();
    // assertEquals(603,archipelago.numOfNonConflConstell());
    // assertEquals(500,archipelago.getVersion(0).value());
    // assertEquals(497,archipelago.getVersion(4).value());
    // for(int i=0; i<10; i++) {
    // try {
    // String file_name = "result_2_"+i+".html";
    // File logFile = new File(File.separator +
    // "C:\\Documents and Settings\\meindert\\Mijn Documenten\\Project Hermans productielijn\\Materiaal input collateX\\output_collatex_exp\\"+file_name);
    // PrintWriter logging = new PrintWriter(new FileOutputStream(logFile));
    // logging.println(buildMatrix.toHtml(archipelago.getVersion(i)));
    // logging.close();
    // } catch (FileNotFoundException e) {
    // e.printStackTrace();
    // }
    // }
  }

  @Test
  public void testHermansText2a() throws XMLStreamException {
    String textD1 = "Op den Atlantischen Oceaan voer een groote stoomer, de lucht was helder blauw, het water rimpelend satijn. Op den Atlantischen Oceaan voer een groote stoomer. Onder de velen aan boojrd bevond zich een bruine, korte dikke man. <i> JSg </i> werd nooit zonder sigaar gezien. Zijn pantalon had lijnrechte vouwen in de pijpen, maar zat toch altijd vol rimpels. <b> De </b> pantalon werd naar boven toe breed, ontzaggelijk breed; hij omsloot den buik van den kleinen man als een soort balcon.";
    String textD9 = "Over de Atlantische Oceaan voer een grote stomer. De lucht was helder blauw, het water rimpelend satijn.<p/> Op de Atlantische Oceaan voer een ontzaggelijk zeekasteel. Onder de vele passagiers aan boord, bevond zich een bruine, korte dikke man. Hij werd nooit zonder sigaar gezien. Zijn pantalon had lijnrechte vouwen in de pijpen, maar zat toch altijd vol rimpels. De pantalon werd naar boven toe breed, ongelofelijk breed: hij omsloot de buik van de kleine man als een soort balkon.";
    SimpleWitness[] witnesses = createWitnesses(textD1, textD9);

    testWitnessCollation(witnesses);
  }

  @Ignore
  @Test
  public void testHermansText2aWithMocking() throws XMLStreamException {
    String textD1 = "Op den Atlantischen Oceaan voer een groote stoomer, de lucht was helder blauw, het water rimpelend satijn. Op den Atlantischen Oceaan voer een groote stoomer. Onder de velen aan boojrd bevond zich een bruine, korte dikke man. <i> JSg </i> werd nooit zonder sigaar gezien. Zijn pantalon had lijnrechte vouwen in de pijpen, maar zat toch altijd vol rimpels. <b> De </b> pantalon werd naar boven toe breed, ontzaggelijk breed; hij omsloot den buik van den kleinen man als een soort balcon.";
    String textD9 = "Over de Atlantische Oceaan voer een grote stomer. De lucht was helder blauw, het water rimpelend satijn.<p/> Op de Atlantische Oceaan voer een ontzaggelijk zeekasteel. Onder de vele passagiers aan boord, bevond zich een bruine, korte dikke man. Hij werd nooit zonder sigaar gezien. Zijn pantalon had lijnrechte vouwen in de pijpen, maar zat toch altijd vol rimpels. De pantalon werd naar boven toe breed, ongelofelijk breed: hij omsloot de buik van de kleine man als een soort balkon.";
    SimpleWitness[] witnesses = createWitnesses(textD1, textD9);

    VariantGraph base = collate(witnesses[0]);
    MatchMatrix matrix = MatchMatrix.create(base, witnesses[1], new EqualityTokenComparator());
    ArchipelagoWithVersions creator = new ArchipelagoWithVersions();
    for (MatchMatrix.Island island : matrix.getIslands()) {
      creator.add(island);
    }

    //Mock Archipelago
    Archipelago result = mock(Archipelago.class);
    Island i1 = new Island(new Coordinate(3, 3), new Coordinate(5, 5));
    Island i2 = new Island(new Coordinate(3, 20), new Coordinate(5, 22));
    Island i3 = new Island(new Coordinate(20, 3), new Coordinate(22, 5));
    Island i4 = new Island(new Coordinate(20, 20), new Coordinate(22, 22));
    Island i5 = new Island(new Coordinate(69, 66), new Coordinate(71, 68));
    Island i6 = new Island(new Coordinate(25, 25), new Coordinate(26, 26));
    Island i7 = new Island(new Coordinate(73, 70), new Coordinate(74, 71));
    Island i8 = new Island(new Coordinate(0, 17), new Coordinate(0, 17));
    Island i9 = new Island(new Coordinate(17, 17), new Coordinate(17, 17));
    when(result.islandsCompete(i1, i2)).thenReturn(true);
    when(result.islandsCompete(i1, i3)).thenReturn(true);
    when(result.islandsCompete(i1, i4)).thenReturn(false);
    when(result.islandsCompete(i1, i5)).thenReturn(false);
    when(result.islandsCompete(i2, i3)).thenReturn(false);
    when(result.islandsCompete(i2, i4)).thenReturn(true);
    when(result.islandsCompete(i2, i5)).thenReturn(false);
    when(result.islandsCompete(i3, i4)).thenReturn(true);
    when(result.islandsCompete(i3, i5)).thenReturn(false);
    when(result.islandsCompete(i4, i5)).thenReturn(false);
    when(result.islandsCompete(i6, i7)).thenReturn(false);
    when(result.islandsCompete(i8, i9)).thenReturn(true);
    when(result.findClosestIsland(i2, i4)).thenReturn(i4);
    when(result.findClosestIsland(i3, i4)).thenReturn(i4);
    when(result.findClosestIsland(i1, i2)).thenReturn(i1);
    when(result.findClosestIsland(i1, i3)).thenReturn(i1);
    when(result.findClosestIsland(i5, i6)).thenReturn(i1);

    creator.createNonConflictingVersion(result);
    verify(result).add(new Island(new Coordinate(40, 39), new Coordinate(58, 57)));
    verify(result).add(new Island(new Coordinate(8, 8), new Coordinate(15, 15)));
    verify(result).add(new Island(new Coordinate(30, 31), new Coordinate(36, 37)));
    verify(result).add(new Island(new Coordinate(62, 59), new Coordinate(67, 64)));
    verify(result).add(new Island(new Coordinate(77, 74), new Coordinate(80, 77)));
    verify(result).islandsCompete(i1, i2);
    verify(result).islandsCompete(i1, i3);
    verify(result).islandsCompete(i1, i4);
    verify(result).islandsCompete(i1, i5);
    verify(result).islandsCompete(i2, i3);
    verify(result).islandsCompete(i2, i4);
    verify(result).islandsCompete(i2, i5);
    verify(result).islandsCompete(i3, i4);
    verify(result).islandsCompete(i3, i5);
    verify(result).islandsCompete(i4, i5);
    verify(result).add(i5);
    verify(result).add(i1);
    verify(result).add(i4);
    verify(result).islandsCompete(i6, i7);
    verify(result).add(i6);
    verify(result).add(i7);
    verify(result).islandsCompete(i8, i9);
    verifyNoMoreInteractions(result);
  }

  private void testWitnessCollation(SimpleWitness[] sw) throws XMLStreamException, FactoryConfigurationError {
    VariantGraph vg = collate(sw);
    String teiMM = generateTEI(vg);
    assertNotNull(teiMM);
    LOG.info(teiMM);

    //    setCollationAlgorithm(CollationAlgorithmFactory.dekker(new EqualityTokenComparator()));
    //    vg = collate(sw);
    //    String teiD = generateTEI(vg);
    //    LOG.info(teiD);
    //    assertNotNull(teiD);
    //    assertFalse(teiD.equals(teiMM));
  }

  @Test
  public void testHermansText3a() throws XMLStreamException {
    String textMZ_DJ233 = "Werumeus Buning maakt artikelen van vijf pagina&APO+s over de geologie van de diepzee, die hij uit Engelse boeken overschrijft, wat hij pas in de laatste regel vermeldt, omdat hij zo goed kan koken.<p/>\nJ. W. Hofstra kan niet lezen en nauwelijks stotteren, laat staan schrijven. Hij oefent het ambt van litterair criticus uit omdat hij uiterlijk veel weg heeft van een Duitse filmacteur (Adolf Wohlbrock).<p/>\nZo nu en dan koopt Elsevier een artikel van een echte professor wiens naam en titels zu vet worden afgedrukt, dat zij allicht de andere copie ook iets professoraals geven, in het oog van de speksnijders.<p/>\nEdouard Bouquin is het olijke culturele geweten. Bouquin betekent: 1) oud boek van geringe waarde, 2) oude bok, 3) mannetjeskonijn. Ik kan het ook niet helpen, het staat in Larousse.<p/>\nDe politiek van dit blad wordt geschreven door een der leeuwen uit het Nederlandse wapen (ik geloof de rechtse) op een krakerige gerechtszaaltoon in zeer korte zinnetjes, omdat hij tot zijn spijt de syntaxis onvoldoende beheerst.<p/>\nAldus de artikelen van Werumeus Buning";
    String textD4F = "Werumeus  Buning maakt machtigmooie artikelen van vijf pagina&APO+s  over de  geologie van de  diepzee, die  hij uit Engelse  boeken overschrijft,   wat hij  pas in de laatste  regel  vermeldt,   omdat hij   zo  goed kan koken.<p/>\nJ. W.Hofstra kan niet lezen en nauwelijks stotteren,   laat staan schrijven.   Hij  oefent het ambt van literair kritikus uit omdat hij uiterlijk veel weg heeft van een Duitse filmacteur (Adolf Wohlbrock).<p/>\nEdouard  Bouquin is  het olijke  culturele  geweten.   Bouquin betekent:   1)  oud boek  van geringe  waarde,   2)  oude bok,   3)  mannetjeskonijn.   Ik kan het ook niet helpen,   het staat in Larousse.<p/>\nNu en dan koopt Elsevier een artikel van een echte professor, wiens naam en titels zu vet worden afgedrukt, dat zij allicht de andere copie ook iets professoraals geven, in het oog van de speksnijders.<p/>\n\nDe politiek van dit blad  wordt geschreven door een der leeuwen uit het nederlandse wapen (ik geloof de   rechtse)  op een krakerige  gerechtszaaltoon in zeer korte  zinnetjes, omdat hij  tot zijn  spijt  de  syntaxis  onvoldoende  beheerst. <p/>Volgens de stukjes van Werumeus Buning";
    SimpleWitness[] sw = createWitnesses(textMZ_DJ233, textD4F);
    testWitnessCollation(sw);
  }

  private String generateTEI(VariantGraph vg) throws XMLStreamException, FactoryConfigurationError {
    SimpleVariantGraphSerializer s = new SimpleVariantGraphSerializer(vg);
    StringWriter writer = new StringWriter();
    vg.join();
    s.toDot(vg, writer);
    //    LOG.info("dot={}", writer.toString());
    XMLStreamWriter xml = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
    s.toTEI(xml);
    return writer.toString();
  }

  @Test
  public void testTEI() throws XMLStreamException, FactoryConfigurationError {
    String text1 = "voor Zo nu en dan zin2 na voor";
    String text2 = "voor zin2 Nu en dan voor";
    SimpleWitness[] sw = createWitnesses(text1, text2);
    testWitnessCollation(sw);
  }
}
