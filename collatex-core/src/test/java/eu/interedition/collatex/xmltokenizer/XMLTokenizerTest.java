package eu.interedition.collatex.xmltokenizer;

import static java.util.stream.Collectors.joining;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.interedition.collatex.simple.SimplePatternTokenizer;

public class XMLTokenizerTest {
    @Test
    public void testXMLTokenizer() {
        String xml1 = "<wit n=\"Wit1\">The "//
                + "<subst xml:id=\"subst-1\">"//
                + "<del hand=\"#AA\">white</del>"//
                + "<add hand=\"#AA\">black</add>"//
                + "</subst>"//
                + " god.</wit>";
        String xml2 = "<wit n=\"Wit1\">The "//
                + "<!-- whatevewr --><app type=\"revision\">"//
                + "<rdg><del hand=\"#AA\">white &amp; purple, or blue and white</del></rdg>"//
                + "<rdg><add hand=\"#AA\">black</add></rdg>"//
                + "<rdg type=\"lit\"><del>white</del><hi rend=\"superscript\"><del>black</del></hi></rdg>"//
                + "</app>"//
                + " god.</wit>";
        String xml3 = "<wit n=\"Wit1\">The"//
                + "<app type=\"revision\">"//
                + "<rdg><del hand=\"AA\">first</del></rdg>"//
                + "<rdg><add hand=\"AA\"><del hand=\"BB\">second</del></add></rdg>"//
                + "<rdg><add hand=\"BB\">third</add></rdg>"//
                + "<rdg type=\"lit\"><strike>first</strike><superscript><strike>second</strike></superscript><superscript>third</superscript></rdg>"//
                + "</app>"//
                + " attempt."//
                + "</wit>";

        XMLTokenizer xt = new XMLTokenizer(xml2);
        Stream<XMLNode> xmlTokenStream = xt.getXMLNodeStream();
        // xmlTokenStream.forEach(xmlnode -> System.out.println(xmlnode.toString()));

        // xmlTokenStream.filter(XMLTextNode.class::isInstance).forEach(xmlnode -> System.out.println(xmlnode.toString()));

        Function<String, Stream<String>> textTokenizer = SimplePatternTokenizer.BY_WS_OR_PUNCT;

        AtomicReference<String> sigilRef = new AtomicReference<>();
        Deque<XMLStartElementNode> openedElements = new ArrayDeque<>();
        xmlTokenStream.forEach(xmlnode -> {
            if (xmlnode instanceof XMLStartElementNode) {
                XMLStartElementNode xmlStartElementNode = (XMLStartElementNode) xmlnode;
                if (xmlStartElementNode.getName().equals("wit")) {
                    sigilRef.set(xmlStartElementNode.getAttributes().get("n"));
                }
                openedElements.push(xmlStartElementNode);
            }

            if (xmlnode instanceof XMLTextNode) {
                XMLTextNode xmlTextNode = (XMLTextNode) xmlnode;
                String text = xmlTextNode.getText();
                List<LayeredTextToken> textTokens = textTokenizer.apply(text)//
                        .map(tokenText -> new LayeredTextToken(sigilRef.get(), tokenText, openedElements))//
                        .collect(Collectors.toList());//
                System.out.println(textTokens.stream().map(LayeredTextToken::toString).collect(joining(";")));

                System.out.println(//
                        ((XMLTextNode) xmlnode).toString() //
                                + " | ancestors = " //
                                + openedElements.stream()//
                                        .map(XMLStartElementNode::toString)//
                                        .collect(joining())//
                );
            }

            if (xmlnode instanceof XMLEndElementNode) {
                openedElements.pop();
            }

        });

    }
}
