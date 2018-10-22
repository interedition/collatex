package eu.interedition.collatex.skipgrams;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

/*
 16-10-2018
 We create n-grams from Tokens of one Witness
 From that we move on to skipgrams of one witness
 Then skipgrams for multiple witnesses and so on and so on.
 */
public class SkipGramMWATest {

    /*
     *
     * We tokenize one witness
     * Then we create b-grams out of them
     */
    @Ignore
    @Test
    public void testNGramsOneWitness() {
        String firstWitness = "a b c d e ";
        SimpleWitness w1 = new SimpleWitness("w1", "a b c d e");
        List<Token> tokens = w1.getTokens();
        System.out.println(tokens);
        NgramCreator c = new NgramCreator();
        ArrayList<List<Token>> ngrams = c.create(tokens, 2);
        System.out.println(ngrams);
    }

    @Test
    public void testSkipGramsOneWitness() {
        SimpleWitness w1 = new SimpleWitness("w1", "a b c d e");
        List<Token> tokens = w1.getTokens();
        System.out.println(tokens);
        SkipgramCreatorKerasStyle c = new SkipgramCreatorKerasStyle();
        List<Skipgram> skipgrams = c.create(tokens, 2);
        System.out.println(skipgrams);

    }

    @Test
    public void testSkipgramsTwoWitnesses() {
        SimpleWitness w1 = new SimpleWitness("w1", "a b c d e");
        List<Token> tokens = w1.getTokens();
        SimpleWitness w2 = new SimpleWitness("w2", "a e c d");
        List<Token> tokens2 = w2.getTokens();
        defineVectorspace(tokens, tokens2);
        throw new RuntimeException("!!");
    }


    public void ontdubbelSkipgramsVoorTweeWitnesses(List<Skipgram> witness1, List<Skipgram> witness2, Comparator<Skipgram> skipgramComparator) {
        // het is beter om een skipgram class te maken
        // NU kunne w het hele ding in een set gooien...
        // Ik word neit
        // Dan moet de equlas method wel geimplementeerd zijn...
        // Of ik moet een amtcher gebruiken
        // MOet; gbruitk de equals van SimpleTOken een extact vergelijk? Waarschijnlijk wel.
        // Nu heb ik een skipgram comparitor nodig
        // Die kan gebruik maken van een token compator door die twee keer toe te passen
        // En
        // Er is een strictqualitytokencomparator...
        // Daar dan een een ngram matcher omheen bouwen?


    }

    public void defineVectorspace(List<Token> witness1, List<Token> witness2) {
        // ik zou denken dat een vector toch gewoon een reeks  van integers is.
        // Waarbij elke positie het aantal voorkomens van een skipgram is.
        // Ik moet dan eerst weten hoeveel unieke voorkomens er in het vocabulaire zijn...
        // Dat verschilt natuurlijk per witness.
        // de vraag is hoe ik makklijk de entris bepaal?
        // een token array creeeren van de witness set in het begin zou alles nog makklijkr makn,
        // maar dat is in feite een optimalisatie.
        // Het gaat om het ontdubbelen van de skipgrams en dan elke skipgram een nummer toekennen..
        // Merk op dat er al dubbelen kunnen voorkomen binnnen de skipgrams van een witness.
        // Dus er is nog een verschil tussen een vocabulair maken van 1 witness en meerdere..
        // We beginnern er met 1
        // er zit geen herhaling in de individuele vectorspace

        // van 1 lijst met skipgrams kun je ook al een vocabulair maken...
        // nu gebeurt dat niet in het voorbeeld waar ik nu mee werk, dus laat maar even..


        // we creeren skipgrams van witness 1 en witness 2
        SkipgramCreatorKerasStyle skipgramCreatorKerasStyle = new SkipgramCreatorKerasStyle();
        List<Skipgram> skipgramsWitness1 = skipgramCreatorKerasStyle.create(witness1, 2);
        List<Skipgram> skipgramsWitness2 = skipgramCreatorKerasStyle.create(witness2, 2);

        // we normalized de twee lijsten...
        List<NormalizedSkipgram> normalizedSkipgrams = skipgramsWitness1.stream().map(sg -> new NormalizedSkipgram(((SimpleToken) sg.head).getNormalized(), ((SimpleToken) sg.tail).getNormalized())).collect(Collectors.toList());
        List<NormalizedSkipgram> normalizedSkipgrams2 = skipgramsWitness2.stream().map(sg -> new NormalizedSkipgram(((SimpleToken) sg.head).getNormalized(), ((SimpleToken) sg.tail).getNormalized())).collect(Collectors.toList());

        System.out.println(normalizedSkipgrams);
        System.out.println(normalizedSkipgrams2);

        // eerst het vocabulair bouwen
        Set<NormalizedSkipgram> vocabulary = new HashSet<>();
        vocabulary.addAll(normalizedSkipgrams);
        vocabulary.addAll(normalizedSkipgrams2);
        System.out.println(vocabulary);
        System.out.println(normalizedSkipgrams.size()+normalizedSkipgrams2.size());
        System.out.println(vocabulary.size());

//        // Het zou eigenlijk een georderde set moeten zijn...
        SortedSet<NormalizedSkipgram> orderedVoca = new TreeSet<>(new NormalizedSkipgramComparator());
        orderedVoca.addAll(vocabulary);
        System.out.println(orderedVoca);

        // Onderstaande code is voor als we willen counten hoe vaak elk item voorkomt..
//        SkipgramComparator myComparator = new SkipgramComparator(new StrictEqualityTokenComparator());
//        Map<Skipgram, Integer> vocabulary = new TreeMap<>(myComparator);
//        vocabulary.compute()



        // ik wil eigenlijk een counter, dan moet het een map met een integer zijn




//        vocabulary.pAll(skipgramsWitness1);
//        vocabulary.addAll(skipgramsWitness2);
//        System.out.println("!!!"+vocabulary);







    }

    public void roadmap() {
        // Nadat we dat voor alle drie de witnesses hebben gedaan
        // moeten we een soort van vocabulair maken...
        // Dat komt toch wel erg op een vector neer...
        // We kunnen twee soorten vectoren maken, 1 vector met daarin hoe vaak skupgrams voorkomen over de hele
        // witness set...
        // En dan vectoren met hoe vaak per witness

    }
}
