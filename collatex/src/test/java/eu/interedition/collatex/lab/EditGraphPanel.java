package eu.interedition.collatex.lab;

import com.google.common.collect.Iterables;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import eu.interedition.collatex.graph.EditOperation;
import eu.interedition.collatex.input.SimpleToken;
import org.apache.commons.collections15.Transformer;

import javax.swing.*;
import java.awt.*;


/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class EditGraphPanel extends VisualizationViewer<EditGraphVertexModel, EditGraphEdgeModel> {

  public EditGraphPanel(EditGraphModel g) {
    super(new SugiyamaLayout<EditGraphVertexModel, EditGraphEdgeModel>(g));

    setBackground(Color.WHITE);
    setBorder(BorderFactory.createTitledBorder("Edit Graph"));
    setGraphMouse(new DefaultModalGraphMouse<String, Integer>());

    final RenderContext<EditGraphVertexModel, EditGraphEdgeModel> rc = getRenderContext();
    rc.setVertexLabelTransformer(new Transformer<EditGraphVertexModel, String>() {
      @Override
      public String transform(EditGraphVertexModel model) {
        if (model.getBase().isEmpty()) {
          return "";
        }

        final SimpleToken bt = (SimpleToken) Iterables.getFirst(model.getBase(), null);
        final SimpleToken wt = (SimpleToken) model.getWitness();
        return String.format("'%s'[%d] = '%s'[%d]", wt.getContent(), wt.getIndex(), bt.getContent(), bt.getIndex());
      }
    });
    rc.setVertexFillPaintTransformer(new Transformer<EditGraphVertexModel, Paint>() {
      @Override
      public Paint transform(EditGraphVertexModel model) {
        return model.getBase().equals(model.getWitness()) ? Color.BLACK : Color.WHITE;
      }
    });
    rc.setEdgeDrawPaintTransformer(new Transformer<EditGraphEdgeModel, Paint>() {
      @Override
      public Paint transform(EditGraphEdgeModel model) {
        return (model.getPaths().isEmpty() ? Color.LIGHT_GRAY : Color.BLACK);
      }
    });
    rc.setEdgeStrokeTransformer(new Transformer<EditGraphEdgeModel, Stroke>() {
      @Override
      public Stroke transform(EditGraphEdgeModel model) {
        return (model.getEditOperation() == EditOperation.GAP ? CollateXLaboratory.DASHED_STROKE : CollateXLaboratory.SOLID_STROKE);
      }
    });
    rc.setEdgeLabelTransformer(new Transformer<EditGraphEdgeModel, String>() {
      @Override
      public String transform(EditGraphEdgeModel model) {
        return Integer.toString(model.getScore().getTempScore());
      }
    });
  }
}
