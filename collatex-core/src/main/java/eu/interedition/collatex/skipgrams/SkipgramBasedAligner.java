package eu.interedition.collatex.skipgrams;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.simple.SimpleToken;

import java.util.*;

/*
 * Skipgram based aligner
 *
 * @author: Ronald Haentjens Dekker
 * @date: 23-10-2018
 *
 * test class to align three witnesses in an order independent manner.
 */
public class SkipgramBasedAligner {
    private Map<String, List<Token>> witnessSet;
    private VariantGraphCreator variantGraphCreator = new VariantGraphCreator();

    public void align(List<Token> witness1, List<Token> witness2, List<Token> witness3) {
        // ik gebruik een map om de witnesss set in op te slaan.
        // ik doe dat nu even hardcoded
        witnessSet = new HashMap<>();
        witnessSet.put("w1", witness1);
        witnessSet.put("w2", witness2);
        witnessSet.put("w3", witness3);

        // We build a skipgram vocabulary based on the three witnesses.
        SkipgramVocabulary vocabulary = new SkipgramVocabulary();
        vocabulary.addWitness(witness1);
        vocabulary.addWitness(witness2);
        vocabulary.addWitness(witness3);

        // we do a fixed number of iteratiros
        // aligning the highest priority each time.
        for (int i=0; i<2; i++) {
            alignTheHighestPriority(vocabulary);
        }
    }

    private void alignTheHighestPriority(SkipgramVocabulary vocabulary) {
        NormalizedSkipgram normalizedSkipgram = vocabulary.selectHighestCount();
        System.out.println("We start/continue with: "+normalizedSkipgram);
        select(normalizedSkipgram);
        System.out.println(variantGraphCreator.toString());
        // remove it and move on
        vocabulary.removeNormalizedSkipgram(normalizedSkipgram);
    }

    public void select(NormalizedSkipgram xxx) {
        // The normalize dskupgram occurs in multiplw witnesses, but in the current construct
        // we don't know in  which witnesses and how many times.
        // Ideally we would be able to find the (token, token) combination in each witness quickly
        // but for now we can just do it with compute.
        // loop over alle witnesses en commit de tokens voor de normalized skipgram!
        // nu maak ik een variant graph creator aan en geef die skupgram mee...
        // ik wil natuurlijk nit steeds een nieuwe aanmaken
        // I create new vertex for every token in a normalized skipgram.sinc skiograms can ovrlap  This is obviously wrong..
        List<String> witnesses = Arrays.asList("w1", "w2", "w3");
        for (String witnessid : witnesses) {
            List<Token> witness1 = witnessSet.get(witnessid);
            // nu create ik die skipgrams opnieuw van een witness
            // en dan pak ik de normalized versoin
            Optional<Skipgram> skipgram = findSkipgramForNormalizedFormInWitness(xxx, witness1);
            if (skipgram.isPresent()) {
                variantGraphCreator.selectSkipgram(skipgram.get());
            }
            // nu gaan we door de naar de volgende witness en zo verder
            // in de toekomst kunnen hierbij transpostions op treden.
            // dan moet we dus iets van een score gaan bijhouden van hoe elk vna deze variant graphs in wording is.
        }


    }

    // Not every witness contains every normalized skipgram.
    // So we return an optional
    private Optional<Skipgram> findSkipgramForNormalizedFormInWitness(NormalizedSkipgram xxx, List<Token> witness1) {
        SkipgramCreator skc = new SkipgramCreator();
        List<Skipgram> skipgrams = skc.create(witness1, 3);
        // we gaan alle skipgrams af op zoek naar een skipgram die voldoet aan die normalzed version
        Optional<Skipgram> result = Optional.empty();
        for (Skipgram skipgram : skipgrams) {
            SimpleToken sthead = (SimpleToken) skipgram.head;
            SimpleToken sttail = (SimpleToken) skipgram.tail;
            NormalizedSkipgram normalizedSkipgram = new NormalizedSkipgram(sthead.getNormalized(), sttail.getNormalized());
            if (normalizedSkipgram.equals(xxx)) {
                result = Optional.of(skipgram);
                break;
            }
        }
        return result;
    }

}
