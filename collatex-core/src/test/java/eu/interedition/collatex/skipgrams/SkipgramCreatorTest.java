package eu.interedition.collatex.skipgrams;


import eu.interedition.collatex.Token;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/*
 * We want to test the more complex skipgrams ..
 * This means longer witnesses..
 * And tri-grams, which means more complex skipgrams patterns..
 * We need statistics and so forth..
 */
public class SkipgramCreatorTest {
    @Test
    public void testIDontFeelLikeIt() {
        String ws1 = "WHEN we look to the individuals of the same variety or sub-variety of our older cultivated plants and animals, one of the first points which strikes us, is, that they generally differ much more from each other, than do the individuals of any one species or variety in a state of nature. When we reflect on the vast diversity of the plants and animals which have been cultivated, and which have varied during all ages under the most different climates and treatment, I think we are driven to conclude that this greater variability is simply due to our domestic productions having been raised under conditions of life not so uniform as, and somewhat different from, those to which the parent-species have been exposed under nature. There is, also, I think, some probability in the view propounded by Andrew Knight, that this variability may be partly connected with excess of food. It seems pretty clear that organic beings must be exposed during several generations to the new conditions of life to cause any appreciable amount of variation; and that when the organisation has once begun to vary, it generally continues to vary for many generations. No case is on record of a variable being ceasing to be variable under cultivation. Our oldest cultivated plants, such as wheat, still often yield new varieties: our oldest domesticated animals are still capable of rapid improvement or modification.";

        // create witness out of strng and tokenize
        SimpleWitness w1 = new SimpleWitness("w1", ws1);
        List<Token> tokens = w1.getTokens();

        // first parameter is how tokens there should be in this in n-gram...
        // second parameter is how large the skipsize should be...
        SkipgramCreator c1 = new SkipgramCreator();
        List<NewSkipgram> skipgrams = c1.secondCreate(tokens, 1, 0);
        System.out.println(skipgrams);

        StaticsCollector collector = new StaticsCollector();
        collector.gather(skipgrams);

        List<Map.Entry<String, Integer>> result = collector.getAllTheNormalizedSkipgramsThatOccurMoreThanOnce();
        System.out.println(result);

    }


}
