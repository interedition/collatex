package eu.interedition.collatex.subst;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

/**
 * Created by ronalddekker on 30/04/16.
 */


public class SubstTokenizerTest {

    @Test
    public void test_tokenizer1() {
        String xml_in = "<wit n=\"1\"><subst><del>In</del><add>At</add></subst> the <subst><del>beginning</del><add>outset</add></subst>, finding the <subst><del>correct</del><add>right</add></subst> word.</wit>";

        SubstTokenizer tokenizer = new SubstTokenizer(xml_in);

        List<XMLToken> tokens = tokenizer.tokenize();

        assertThat(tokens.get(0), hasProperty("text", equalTo("In")));
        assertThat(tokens.get(0), hasProperty("open_tags", equalTo(Arrays.asList("wit","subst", "del"))));
        // TODO: assert end tags (del)
    }


}
