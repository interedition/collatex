package eu.interedition.collatex.lab;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import eu.interedition.collatex.implementation.graph.EditOperation;
import eu.interedition.collatex.implementation.input.SimpleToken;
import eu.interedition.collatex.interfaces.IWitness;
import eu.interedition.collatex.interfaces.Token;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import javax.swing.*;
import java.awt.*;
import java.util.SortedSet;


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
        final SimpleToken bt = (SimpleToken) model.getBase();
        final SimpleToken wt = (SimpleToken) model.getWitness();
        if (bt.equals(wt)) {
          return "";
        }
        return String.format("'%s'[%d] = '%s'[%d]", bt.getContent(), bt.getIndex(), wt.getContent(), wt.getIndex());
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
        return Integer.toString(model.getScore());
      }
    });
  }
}
