package eu.interedition.collatex.MatrixLinker;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.matching.EqualityTokenComparator;

public class HermansTest extends AbstractTest {

  @Ignore
  @Test
  public void testHermansText1() {
  	String textD1 = "Op den Atlantischen Oceaan voer een groote stoomer, de lucht was helder blauw, het water rimpelend satijn.";
  	String textD9 = "Over de Atlantische Oceaan voer een grote stomer. De lucht was helder blauw, het water rimpelend satijn.<p/>";
  	SimpleWitness[] sw = createWitnesses(textD1,textD9);
		VariantGraph vg = collate(sw[0]);
  	MatchMatrixLinker linker = new MatchMatrixLinker();
  	MatchMatrix buildMatrix = linker.buildMatrix(vg,sw[1],new EqualityTokenComparator());
  	System.out.println(buildMatrix.toHtml());
  }
  
  @Ignore
  @Test
  public void testHermansText2() {
  	String textD1 = "Op den Atlantischen Oceaan voer een groote stoomer. Onder de velen aan boojrd bevond zich een bruine, korte dikke man. <i> JSg </i> werd nooit zonder sigaar gezien. Zijn pantalon had lijnrechte vouwen in de pijpen, maar zat toch altijd vol rimpels. <b> De </b> pantalon werd naar boven toe breed, ontzaggelijk breed; hij omsloot den buik van den kleinen man als een soort balcon.";
  	String textD9 = "Op de Atlantische Oceaan voer een ontzaggelijk zeekasteel. Onder de vele passagiers aan boord, bevond zich een bruine, korte dikke man. Hij werd nooit zonder sigaar gezien. Zijn pantalon had lijnrechte vouwen in de pijpen, maar zat toch altijd vol rimpels. De pantalon werd naar boven toe breed, ongelofelijk breed: hij omsloot de buik van de kleine man als een soort balkon.";
  	SimpleWitness[] sw = createWitnesses(textD1,textD9);
		VariantGraph vg = collate(sw[0]);
  	MatchMatrixLinker linker = new MatchMatrixLinker();
  	MatchMatrix buildMatrix = linker.buildMatrix(vg,sw[1],new EqualityTokenComparator());
//  	System.out.println(buildMatrix.toHtml());
  	ArchipelagoWithVersions archipelago = new ArchipelagoWithVersions();
  	for(MatchMatrix.Island isl: buildMatrix.getIslands())	{
  		archipelago.add(isl);
    }
  	System.out.println("archipelago: "+ archipelago);
  	System.out.println("archipelago.size(): "+ archipelago.size());
  	assertEquals(42, archipelago.size());
    assertEquals(98,archipelago.numOfConflicts());
//  	assertTrue(false);
//    archipelago.createNonConflictingVersions();
//    assertEquals(603,archipelago.numOfNonConflConstell());
//    assertEquals(500,archipelago.getVersion(0).value());
//    assertEquals(497,archipelago.getVersion(4).value());
    
    Archipelago firstVersion = archipelago.createFirstVersion();
  	for(MatchMatrix.Island isl: firstVersion.iterator()) {
  		System.out.print(" "+isl.size());
  	}
  	try {
  		int i = 0;
  		String file_name = "result_3_"+i+".html";
    	File logFile = new File(File.separator + "C:\\Documents and Settings\\meindert\\Mijn Documenten\\Project Hermans productielijn\\Materiaal input collateX\\output_collatex_exp\\"+file_name);
    	PrintWriter logging = new PrintWriter(new FileOutputStream(logFile));
//  	logging.println(buildMatrix.toHtml(archipelago.getVersion(i)));
    	logging.println(buildMatrix.toHtml(firstVersion));
    	logging.close();
  	} catch (FileNotFoundException e) {
  		e.printStackTrace();
  	}
//    for(int i=0; i<10; i++) {
//  		try {
//  			String file_name = "result_"+i+".html";
//    		File logFile = new File(File.separator + "C:\\Documents and Settings\\meindert\\Mijn Documenten\\Project Hermans productielijn\\Materiaal input collateX\\output_collatex_exp\\"+file_name);
//    		PrintWriter logging = new PrintWriter(new FileOutputStream(logFile));
//      	logging.println(buildMatrix.toHtml(archipelago.getVersion(i)));
//      	logging.close();
//      } catch (FileNotFoundException e) {
//  	    e.printStackTrace();
//      }
//    }

  }

  @Test
  public void testHermansText3() {
  	String textMZ_DJ233 = "Werumeus Buning maakt artikelen van vijf pagina&APO+s over de geologie van de diepzee, die hij uit Engelse boeken overschrijft, wat hij pas in de laatste regel vermeldt, omdat hij zo goed kan koken.<p/>\n" +
"J. W. Hofstra kan niet lezen en nauwelijks stotteren, laat staan schrijven. Hij oefent het ambt van litterair criticus uit omdat hij uiterlijk veel weg heeft van een Duitse filmacteur (Adolf Wohlbrock).<p/>\n" +
"Zo nu en dan koopt Elsevier een artikel van een echte professor wiens naam en titels zu vet worden afgedrukt, dat zij allicht de andere copie ook iets professoraals geven, in het oog van de speksnijders.<p/>\n" +
"Edouard Bouquin is het olijke culturele geweten. Bouquin betekent: 1) oud boek van geringe waarde, 2) oude bok, 3) mannetjeskonijn. Ik kan het ook niet helpen, het staat in Larousse.<p/>\n" +
"De politiek van dit blad wordt geschreven door een der leeuwen uit het Nederlandse wapen (ik geloof de rechtse) op een krakerige gerechtszaaltoon in zeer korte zinnetjes, omdat hij tot zijn spijt de syntaxis onvoldoende beheerst.<p/>\n";
  	String textD4F ="Werumeus  Buning maakt artikelen van vijf pagina&APO+s  over de  geologie van de  diepzee, die  hij uit Engelse  boeken overschrijft,   wat hij  pas in de laatste  regel  vermeldt,   omdat hij   zo  goed kan koken.<p/>\n"+
"J. W.Hofstra kan niet lezen en nauwelijks stotteren,   laat staan schrijven.   Hij  oefent het ambt van literair kritikus uit omdat hij uiterlijk veel weg heeft van een Duitse filmacteur (Adolf Wohlbrock).<p/>\n" +
"Edouard  Bouquin is  het olijke  culturele  geweten.   Bouquin betekent:   1)  oud boek  van geringe  waarde,   2)  oude bok,   3)  mannetjeskonijn.   Ik kan het ook niet helpen,   het staat in Larousse.<p/>\n" +
"Nu en dan koopt Elsevier een artikel van een echte professor, wiens naam en titels zu vet worden afgedrukt, dat zij allicht de andere copie ook iets professoraals geven, in het oog van de speksnijders.<p/>\n" +
"\n" +
"De politiek van dit blad  wordt geschreven door een der leeuwen uit het nederlandse wapen (ik geloof de   rechtse)  op een krakerige  gerechtszaaltoon in zeer korte  zinnetjes, omdat hij  tot zijn  spijt  de  syntaxis  onvoldoende  beheerst. <p/>";
  	SimpleWitness[] sw = createWitnesses(textMZ_DJ233,textD4F);
		VariantGraph vg = collate(sw[0]);
  	MatchMatrixLinker linker = new MatchMatrixLinker();
  	MatchMatrix buildMatrix = linker.buildMatrix(vg,sw[1],new EqualityTokenComparator());
//  	try {
//	    FileWriter fw = new FileWriter("C:\\Documents and Settings\\meindert\\Mijn Documenten\\Project Hermans productielijn\\Materiaal input collateX\\Hulp1.html");
//	    fw.write(buildMatrix.toHtml());
//    } catch (IOException e) {
//	    e.printStackTrace();
//    }
//  	System.out.println(buildMatrix.toHtml());
  	ArchipelagoWithVersions archipelago = new ArchipelagoWithVersions();
  	for(MatchMatrix.Island isl: buildMatrix.getIslands())	{
  		archipelago.add(isl);
    }
  	System.out.println("archipelago: "+ archipelago);
  	System.out.println("archipelago.size(): "+ archipelago.size());
  	for(MatchMatrix.Island isl: archipelago.iterator()) {
  		System.out.print(" "+isl.size());
  	}
  	System.out.println();
  	assertEquals(233, archipelago.size());
    assertEquals(1429,archipelago.numOfConflicts());
    Archipelago firstVersion = archipelago.createFirstVersion();
  	for(MatchMatrix.Island isl: firstVersion.iterator()) {
  		System.out.print(" "+isl.size());
  	}
  	try {
  		int i = 0;
  		String file_name = "result_2_"+i+".html";
    	File logFile = new File(File.separator + "C:\\Documents and Settings\\meindert\\Mijn Documenten\\Project Hermans productielijn\\Materiaal input collateX\\output_collatex_exp\\"+file_name);
    	PrintWriter logging = new PrintWriter(new FileOutputStream(logFile));
//  	logging.println(buildMatrix.toHtml(archipelago.getVersion(i)));
    	logging.println(buildMatrix.toHtml(firstVersion));
    	logging.close();
  	} catch (FileNotFoundException e) {
  		e.printStackTrace();
  	}
    
    assertEquals(4877, firstVersion.value());
//  	assertTrue(false);

//    archipelago.createNonConflictingVersions();
//    assertEquals(603,archipelago.numOfNonConflConstell());
//    assertEquals(500,archipelago.getVersion(0).value());
//    assertEquals(497,archipelago.getVersion(4).value());
//    for(int i=0; i<10; i++) {
//  		try {
//  			String file_name = "result_2_"+i+".html";
//    		File logFile = new File(File.separator + "C:\\Documents and Settings\\meindert\\Mijn Documenten\\Project Hermans productielijn\\Materiaal input collateX\\output_collatex_exp\\"+file_name);
//    		PrintWriter logging = new PrintWriter(new FileOutputStream(logFile));
//      	logging.println(buildMatrix.toHtml(archipelago.getVersion(i)));
//      	logging.close();
//      } catch (FileNotFoundException e) {
//  	    e.printStackTrace();
//      }
//    }
  }
  
}
