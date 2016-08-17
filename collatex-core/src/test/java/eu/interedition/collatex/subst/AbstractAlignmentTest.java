package eu.interedition.collatex.subst;

import com.google.common.collect.Lists;
import de.vandermeer.asciitable.v2.RenderedTable;
import de.vandermeer.asciitable.v2.V2_AsciiTable;
import de.vandermeer.asciitable.v2.render.V2_AsciiTableRenderer;
import de.vandermeer.asciitable.v2.render.WidthLongestWord;
import de.vandermeer.asciitable.v2.themes.V2_E_TableThemes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by ronalddekker on 15/08/16.
 */
public class AbstractAlignmentTest {
    protected void visualizeSuperWitness(List<List<WitnessNode>> superWitness) {
        List<Object> witATokens = Lists.newArrayList("A:");
        List<Object> witABTokens = Lists.newArrayList("A+B:");
        List<Object> witBTokens = Lists.newArrayList("B:");

        superWitness.forEach(l -> {
            // System.err.println("l=" + l);
            WitnessNode witnessNode = l.get(0);
            String witnessNodeText = witnessNodeText(witnessNode);
            if (l.size() == 2) {
                witATokens.add("");
                witABTokens.add(witnessNodeText);
                witBTokens.add("");

            } else if (l.size() == 1) {
                witABTokens.add("");
                if ("A".equals(witnessNode.getSigil())) {
                    witATokens.add(witnessNodeText);
                    witBTokens.add("");
                } else if ("B".equals(witnessNode.getSigil())) {
                    witATokens.add("");
                    witBTokens.add(witnessNodeText);
                }

            }
        });

        V2_AsciiTable table = new V2_AsciiTable();
        table.addRule();
        addRow(table, witATokens, 'c');
        addRow(table, witABTokens, 'c');
        addRow(table, witBTokens, 'c');
        printTable(table);

    }

    protected String witnessNodeText(WitnessNode witnessNode) {
        return witnessNode.data.replace(" ", "\u2022");
    }

    protected void addRow(V2_AsciiTable at, List<Object> row, char alignment) {
        at.addRow(row.toArray()).setAlignment(alignment(row, alignment));
        at.addRule();
    }

    private char[] alignment(List<Object> row, char alignmentType) {
        char[] a = new char[row.size()];
        Arrays.fill(a, alignmentType);
        return a;
    }

    protected void printTable(V2_AsciiTable table) {
        RenderedTable rt = new V2_AsciiTableRenderer()//
                .setTheme(V2_E_TableThemes.UTF_LIGHT.get())//
                .setWidth(new WidthLongestWord())//
                .render(table);
        System.out.println(rt);
    }

    protected List<Integer> convertWitnessNodeRankMapToList(Map<WitnessNode, Integer> witnessNodeToRank, List<List<WitnessNode>> superWitness) {
        List<WitnessNode> superwitnessflattened = new ArrayList<>();
        superWitness.stream().forEach( l -> { l.stream().forEach( n -> {
                superwitnessflattened.add(n);
            });
        });
        List<Integer> result = new ArrayList<>();
        superwitnessflattened.forEach(n -> {
            result.add(witnessNodeToRank.get(n));
        });
        return result;
    }
}
