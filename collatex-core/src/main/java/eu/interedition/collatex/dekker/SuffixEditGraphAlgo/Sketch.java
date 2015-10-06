package eu.interedition.collatex.dekker.suffixeditgraphalgo;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.dekker.experimental_aligner.Dekker21Aligner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ronalddekker on 30/08/15.
 */
public class Sketch {

    // every node in the decision tree needs to be connected to the tokenindex
    private Dekker21Aligner.TokenIndex tokenIndex;

    // constructor from a superbase and a witness
    public static Dekker21Aligner.TokenIndex fromSuperbaseAndWitness(List<Token> superbase, Iterable<Token> witness) {
        // voor nu bouw ik de token index hier op.
        // later kan ik dat dan wel wijzigen
        // de token index is eigenlijk alleen relevant voor een decision tree aanpak
        // dus de verantwoordelijkheid moet hier uit gemoved worden..
        // Ik combineer superbase en witness met elkaar


        // combineer de beide zaken
        List<Iterable<Token>> witnesses = new ArrayList<>();
        witnesses.add(superbase);
        witnesses.add(copyIterator(witness));
        Dekker21Aligner.TokenIndex tokenIndex = new Dekker21Aligner.TokenIndex(witnesses);
        return tokenIndex;
    }

    public static <T> List<T> copyIterator(Iterable<T> ible) {
        Iterator<T> iter = ible.iterator();
        List<T> copy = new ArrayList<T>();
        while (iter.hasNext())
            copy.add(iter.next());
        return copy;
    }



}
