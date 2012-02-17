package eu.interedition.collatex.MatrixLinker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.input.SimpleWitness;
import eu.interedition.collatex.matching.EqualityTokenComparator;

public class HermansTest extends AbstractTest {

  @Ignore
  @Test
  public void testHermansText1() {
  	String textD1 = "Op den Atlantischen Oceaan voer een groote stoomer, de lucht was helder blauw, het water rimpelend satijn.";
  	String textD9 = "Over de Atlantische Oceaan voer een grote stomer. De lucht was helder blauw, het water rimpelend satijn.<p/>";
  	SimpleWitness[] sw = createWitnesses(textD1,textD9);
		VariantGraph vg = collate(sw[0]);
  	MatrixLinker linker = new MatrixLinker();
  	SparseMatrix buildMatrix = linker.buildMatrix(vg,sw[1],new EqualityTokenComparator());
  	System.out.println(buildMatrix.toHtml());
  }
  
  @Ignore
  @Test
  public void testHermansText2() {
  	String textD1 = "Op den Atlantischen Oceaan voer een groote stoomer. Onder de velen aan boojrd bevond zich een bruine, korte dikke man. <i> JSg </i> werd nooit zonder sigaar gezien. Zijn pantalon had lijnrechte vouwen in de pijpen, maar zat toch altijd vol rimpels. <b> De </b> pantalon werd naar boven toe breed, ontzaggelijk breed; hij omsloot den buik van den kleinen man als een soort balcon.";
  	String textD9 = "Op de Atlantische Oceaan voer een ontzaggelijk zeekasteel. Onder de vele passagiers aan boord, bevond zich een bruine, korte dikke man. Hij werd nooit zonder sigaar gezien. Zijn pantalon had lijnrechte vouwen in de pijpen, maar zat toch altijd vol rimpels. De pantalon werd naar boven toe breed, ongelofelijk breed: hij omsloot de buik van de kleine man als een soort balkon.";
  	SimpleWitness[] sw = createWitnesses(textD1,textD9);
		VariantGraph vg = collate(sw[0]);
  	MatrixLinker linker = new MatrixLinker();
  	SparseMatrix buildMatrix = linker.buildMatrix(vg,sw[1],new EqualityTokenComparator());
//  	System.out.println(buildMatrix.toHtml());
  	ArchipelagoWithVersions archipelago = new ArchipelagoWithVersions();
  	for(Island isl: buildMatrix.getIslands())	{
  		archipelago.add(isl);
    }
  	System.out.println("archipelago: "+ archipelago);
  	System.out.println("archipelago.size(): "+ archipelago.size());
  	assertEquals(42, archipelago.size());
    assertEquals(98,archipelago.numOfConflicts());
//  	assertTrue(false);
    archipelago.createNonConflictingVersions();
    assertEquals(603,archipelago.numOfNonConflConstell());
    assertEquals(500,archipelago.getVersion(0).value());
    assertEquals(497,archipelago.getVersion(4).value());
  }
  
  @Test
  public void testHermansText3() {
  	String textMZ_DJ233 = "Werumeus Buning maakt artikelen van vijf pagina&APO+s over de geologie van de diepzee, die hij uit Engelse boeken overschrijft, wat hij pas in de laatste regel vermeldt, omdat hij zo goed kan koken.<p/>\n" +
"J. W. Hofstra kan niet lezen en nauwelijks stotteren, laat staan schrijven. Hij oefent het ambt van litterair criticus uit omdat hij uiterlijk veel weg heeft van een Duitse filmacteur (Adolf Wohlbrück).<p/>\n" +
"Zo nu en dan koopt Elsevier een artikel van een echte professor wiens naam en titels zó vet worden afgedrukt, dat zij allicht de andere copie ook iets professoraals geven, in het oog van de speksnijders.<p/>\n" +
"Edouard Bouquin is het olijke culturele geweten. Bouquin betekent: 1) oud boek van geringe waarde, 2) oude bok, 3) mannetjeskonijn. Ik kan het ook niet helpen, het staat in Larousse.<p/>\n" +
"De politiek van dit blad wordt geschreven door een der leeuwen uit het Nederlandse wapen (ik geloof de rechtse) op een krakerige gerechtszaaltoon in zeer korte zinnetjes, omdat hij tot zijn spijt de syntaxis onvoldoende beheerst.<p/>\n";
  	String textD4F ="Werumeus  Buning maakt artikelen van vijf pagina&APO+s  over de  geologie van de  diepzee, die  hij uit Engelse  boeken overschrijft,   wat hij  pas in de laatste  regel  vermeldt,   omdat hij   zo  goed kan koken.<p/>\n"+
"J. W.Hofstra kan niet lezen en nauwelijks stotteren,   laat staan schrijven.   Hij  oefent het ambt van literair kritikus uit omdat hij uiterlijk veel weg heeft van een Duitse filmacteur (Adolf Wohlbrück).<p/>\n" +
"Edouard  Bouquin is  het olijke  culturele  geweten.   Bouquin betekent:   1)  oud boek  van geringe  waarde,   2)  oude bok,   3)  mannetjeskonijn.   Ik kan het ook niet helpen,   het staat in Larousse.<p/>\n" +
"Nu en dan koopt Elsevier een artikel van een echte professor, wiens naam en titels zó vet worden afgedrukt, dat zij allicht de andere copie ook iets professoraals geven, in het oog van de speksnijders.<p/>\n" +
"\n" +
"De politiek van dit blad  wordt geschreven door een der leeuwen uit het nederlandse wapen (ik geloof de   rechtse)  op een krakerige  gerechtszaaltoon in zeer korte  zinnetjes, omdat hij  tot zijn  spijt  de  syntaxis  onvoldoende  beheerst. <p/>";
  	SimpleWitness[] sw = createWitnesses(textMZ_DJ233,textD4F);
		VariantGraph vg = collate(sw[0]);
  	MatrixLinker linker = new MatrixLinker();
  	SparseMatrix buildMatrix = linker.buildMatrix(vg,sw[1],new EqualityTokenComparator());
  	ArchipelagoWithVersions archipelago = new ArchipelagoWithVersions();
  	for(Island isl: buildMatrix.getIslands())	{
  		archipelago.add(isl);
    }
  	System.out.println("archipelago: "+ archipelago);
  	System.out.println("archipelago.size(): "+ archipelago.size());
  	for(Island isl: archipelago.iterator()) {
  		System.out.print(" "+isl.size());
  	}
  	System.out.println();
  	assertEquals(233, archipelago.size());
    assertEquals(1429,archipelago.numOfConflicts());
    Archipelago firstVersion = archipelago.createFirstVersion();
  	for(Island isl: firstVersion.iterator()) {
  		System.out.print(" "+isl.size());
  	}
    
    assertEquals(4877, firstVersion.value());
//  	assertTrue(false);

//    archipelago.createNonConflictingVersions();
//    assertEquals(603,archipelago.numOfNonConflConstell());
//    assertEquals(500,archipelago.getVersion(0).value());
//    assertEquals(497,archipelago.getVersion(4).value());
//    }
  }
  
}
