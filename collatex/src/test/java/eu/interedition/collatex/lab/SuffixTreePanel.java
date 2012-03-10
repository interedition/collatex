package eu.interedition.collatex.lab;

import com.google.common.collect.Iterables;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import eu.interedition.collatex.simple.SimpleToken;
import org.apache.commons.collections15.Transformer;

import java.awt.*;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SuffixTreePanel extends VisualizationViewer<SuffixTreeVertexModel, SuffixTreeEdgeModel> {

  public SuffixTreePanel() {
    super(new StaticLayout(new DelegateTree()));
    setBackground(Color.WHITE);
    setGraphMouse(new DefaultModalGraphMouse<String, Integer>());

    final RenderContext<SuffixTreeVertexModel, SuffixTreeEdgeModel> rc = getRenderContext();
    rc.setVertexLabelTransformer(new Transformer<SuffixTreeVertexModel, String>() {
      @Override
      public String transform(SuffixTreeVertexModel model) {
        return String.format("%d: %s", model.getPathPosition(), SimpleToken.toString(model.getTokens()));
      }
    });
    rc.setVertexFillPaintTransformer(new Transformer<SuffixTreeVertexModel, Paint>() {
      @Override
      public Paint transform(SuffixTreeVertexModel suffixTreeVertexModel) {
        return Color.BLACK;
      }
    });
  }
}
