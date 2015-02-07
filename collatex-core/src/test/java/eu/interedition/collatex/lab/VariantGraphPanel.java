/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.lab;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.util.VariantGraphRanking;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.WordUtils;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;

import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.simple.SimpleToken;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphPanel extends VisualizationViewer<VariantGraph.Vertex, VariantGraph.Edge> {

  private VariantGraph variantGraph;
  private VariantGraphRanking ranking;
  private Map<VariantGraph.Transposition, Color> transpositionColors;

  public VariantGraphPanel(VariantGraph vg) {
    super(new StaticLayout<>(new VariantGraph()));

    setBackground(Color.WHITE);
    setGraphMouse(new DefaultModalGraphMouse<String, Integer>());

    final RenderContext<VariantGraph.Vertex, VariantGraph.Edge> rc = getRenderContext();
    rc.setVertexLabelTransformer(new Transformer<VariantGraph.Vertex, String>() {
      @Override
      public String transform(VariantGraph.Vertex variantGraphVertexModel) {
        final Multimap<Witness, Token> tokens = Multimaps.index(variantGraphVertexModel.tokens(), Token::getWitness);
        final StringBuilder label = new StringBuilder();
        for (Witness witness : Ordering.from(Witness.SIGIL_COMPARATOR).sortedCopy(tokens.keySet())) {
          label.append("[").append(witness.getSigil()).append(": '");
          for (Iterator<SimpleToken> tokenIt = Ordering.natural().sortedCopy(Iterables.filter(tokens.get(witness), SimpleToken.class)).iterator(); tokenIt.hasNext(); ) {
            label.append(tokenIt.next().getContent());
            if (tokenIt.hasNext()) {
              label.append(" ");
            }
          }
          label.append("']\n");
        }
        String trim = label.append("(").append(Objects.firstNonNull(ranking.apply(variantGraphVertexModel), 0)).append(")").toString().trim();
        String wrappedLabel = WordUtils.wrap(trim, 30, "\n", false);
        String htmllabel = StringEscapeUtils.escapeHtml(wrappedLabel).replaceAll("\n", "<br/>");
        return "<html>" + htmllabel + "</html>";
      }
    });
    rc.setEdgeLabelTransformer(new Transformer<VariantGraph.Edge, String>() {
      @Override
      public String transform(VariantGraph.Edge variantGraphEdgeModel) {
        return Joiner.on(", ").join(Iterables.transform(variantGraphEdgeModel.witnesses(), new Function<Witness, String>() {

          @Override
          public String apply(Witness input) {
            return input.getSigil();
          }
        }));
      }
    });
    rc.setVertexFillPaintTransformer(new Transformer<VariantGraph.Vertex, Paint>() {
      @Override
      public Paint transform(VariantGraph.Vertex v) {
        final VariantGraph.Transposition transposition = Iterables.getFirst(v.transpositions(), null);

        return (v.tokens().isEmpty() ? Color.BLACK : (transposition == null
                ? Color.WHITE
                : transpositionColors.get(transposition)
        ));
      }
    });
    rc.setEdgeStrokeTransformer(new Transformer<VariantGraph.Edge, Stroke>() {
      @Override
      public Stroke transform(VariantGraph.Edge variantGraphEdgeModel) {
        return variantGraphEdgeModel.witnesses().isEmpty() ? CollateXLaboratory.DASHED_STROKE : CollateXLaboratory.SOLID_STROKE;
      }
    });
    rc.setEdgeDrawPaintTransformer(new Transformer<VariantGraph.Edge, Paint>() {
      @Override
      public Paint transform(VariantGraph.Edge jungVariantGraphEdge) {
        return Color.GRAY;
      }
    });

    setVariantGraph(vg);
  }

  public void setVariantGraph(VariantGraph variantGraph) {
    this.variantGraph = variantGraph;
    this.ranking = VariantGraphRanking.of(variantGraph);

    this.transpositionColors = Maps.newHashMap();
    int tc = 0;
    for (VariantGraph.Transposition transposition : variantGraph.transpositions()) {
      this.transpositionColors.put(transposition, KELLY_MAX_CONTRAST_COLORS[tc++ % KELLY_MAX_CONTRAST_COLORS.length]);
    }
    setGraphLayout(new VariantGraphLayoutAdapter(variantGraph, VariantGraphLayoutAdapter.Orientation.LEFT, 300, 150));
    revalidate();
  }

  private static Color[] KELLY_MAX_CONTRAST_COLORS = new Color[]{
          new Color(0xFFFFB300), //Vivid Yellow
          new Color(0xFF803E75), //Strong Purple
          new Color(0xFFFF6800), //Vivid Orange
          new Color(0xFFA6BDD7), //Very Light Blue
          new Color(0xFFC10020), //Vivid Red
          new Color(0xFFCEA262), //Grayish Yellow
          new Color(0xFF817066), //Medium Gray

          //The following will not be good for people with defective color vision
          new Color(0xFF007D34), //Vivid Green
          new Color(0xFFF6768E), //Strong Purplish Pink
          new Color(0xFF00538A), //Strong Blue
          new Color(0xFFFF7A5C), //Strong Yellowish Pink
          new Color(0xFF53377A), //Strong Violet
          new Color(0xFFFF8E00), //Vivid Orange Yellow
          new Color(0xFFB32851), //Strong Purplish Red
          new Color(0xFFF4C800), //Vivid Greenish Yellow
          new Color(0xFF7F180D), //Strong Reddish Brown
          new Color(0xFF93AA00), //Vivid Yellowish Green
          new Color(0xFF593315), //Deep Yellowish Brown
          new Color(0xFFF13A13), //Vivid Reddish Orange
          new Color(0xFF232C16), //Dark Olive Green
  };
}
