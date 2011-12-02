package eu.interedition.collatex.lab;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import edu.uci.ics.jung.algorithms.layout.DAGLayout;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IWitness;
import org.apache.commons.collections15.Transformer;

import java.awt.Color;
import java.awt.Paint;


/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphPanel extends VisualizationViewer<VariantGraphVertex, VariantGraphEdge> {

  public VariantGraphPanel(VariantGraph g) {
    super(new DAGLayout<VariantGraphVertex, VariantGraphEdge>(g));

    setBackground(Color.WHITE);
    setGraphMouse(new DefaultModalGraphMouse<String, Integer>());

    final RenderContext<VariantGraphVertex, VariantGraphEdge> rc = getRenderContext();
    rc.setEdgeLabelTransformer(new Transformer<VariantGraphEdge, String>() {
      @Override
      public String transform(VariantGraphEdge variantGraphEdge) {
        return Joiner.on(", ").join(Iterables.transform(variantGraphEdge.getWitnesses(), new Function<IWitness, String>() {

          @Override
          public String apply(IWitness input) {
            return input.getSigil();
          }
        }));
      }
    });
    rc.setVertexLabelTransformer(new Transformer<VariantGraphVertex, String>() {
      @Override
      public String transform(VariantGraphVertex variantGraphVertex) {
        return Joiner.on(", ").join(Iterables.transform(variantGraphVertex.getTokens(), new Function<INormalizedToken, String>() {

          @Override
          public String apply(INormalizedToken input) {
            return input.getWitness().getSigil() + ":'" + input.getNormalized() + "'";
          }
        }));
      }
    });
    rc.setVertexFillPaintTransformer(new Transformer<VariantGraphVertex, Paint>() {
      @Override
      public Paint transform(VariantGraphVertex v) {
        return v.getTokens().isEmpty() ? Color.BLACK : Color.WHITE;
      }
    });
  }
}
