package eu.interedition.collatex.lab;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import eu.interedition.collatex.implementation.alignment.VariantGraphWitnessAdapter;
import eu.interedition.collatex.implementation.input.SimpleToken;
import eu.interedition.collatex.interfaces.Token;
import eu.interedition.collatex.interfaces.IWitness;
import org.apache.commons.collections15.Transformer;

import java.awt.*;


/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphPanel extends VisualizationViewer<VariantGraphVertex, VariantGraphEdge> {

  public static final BasicStroke TRANSPOSITION_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] { 10.0f }, 0.0f);
  public static final BasicStroke PATH_STROKE = new BasicStroke();

  public VariantGraphPanel(VariantGraph g) {
    super(new FRLayout<VariantGraphVertex, VariantGraphEdge>(g));

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
        return Joiner.on(", ").join(Iterables.transform(variantGraphVertex.getTokens(), new Function<Token, String>() {

          @Override
          public String apply(Token input) {
            return input.getWitness().getSigil() + ":'" + ((SimpleToken) input).getNormalized() + "'";
          }
        })) + " (" + variantGraphVertex.getRank() + ")";
      }
    });
    rc.setEdgeStrokeTransformer(new Transformer<VariantGraphEdge, Stroke>() {
      @Override
      public Stroke transform(VariantGraphEdge variantGraphEdge) {
        return variantGraphEdge.getWitnesses().isEmpty() ? TRANSPOSITION_STROKE : PATH_STROKE;
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
