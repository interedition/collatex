package eu.interedition.collatex.xmltokenizer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

public class LayeredTextTokenizer {

    private Stream<XMLNode> xmlNodeStream;
    private Function<String, Stream<String>> textTokenizer;

    public LayeredTextTokenizer(String xml, Function<String, Stream<String>> textTokenizer) {
        this.textTokenizer = textTokenizer;
        this.xmlNodeStream = new XMLTokenizer(xml).getXMLNodeStream();
    }

    public List<LayeredTextToken> getLayeredTextTokens() {
        AtomicReference<String> sigilRef = new AtomicReference<>();
        Deque<XMLStartElementNode> openedElements = new ArrayDeque<>();
        List<LayeredTextToken> tokens = Lists.newArrayList();
        this.xmlNodeStream.forEach(xmlnode -> {
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
                List<LayeredTextToken> textTokens = this.textTokenizer.apply(text)//
                        .map(tokenText -> new LayeredTextToken(sigilRef.get(), tokenText, openedElements))//
                        .collect(Collectors.toList());//

                tokens.addAll(textTokens);
            }

            if (xmlnode instanceof XMLEndElementNode) {
                openedElements.pop();
            }

        });
        return tokens;

    }
}
