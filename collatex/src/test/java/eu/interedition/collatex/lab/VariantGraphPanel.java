package eu.interedition.collatex.lab;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.input.SimpleToken;
import org.apache.commons.collections15.Transformer;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;


/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class VariantGraphPanel extends VisualizationViewer<VariantGraphVertexModel, VariantGraphEdgeModel> {

  public VariantGraphPanel(VariantGraphModel g) {
    super(new SugiyamaLayout<VariantGraphVertexModel, VariantGraphEdgeModel>(g));

    setBackground(Color.WHITE);
    setBorder(BorderFactory.createTitledBorder("Variant Graph"));
    setGraphMouse(new DefaultModalGraphMouse<String, Integer>());

    final RenderContext<VariantGraphVertexModel, VariantGraphEdgeModel> rc = getRenderContext();
    rc.setVertexLabelTransformer(new Transformer<VariantGraphVertexModel, String>() {
      @Override
      public String transform(VariantGraphVertexModel variantGraphVertexModel) {
        final Multimap<Witness,Token> tokens = Multimaps.index(variantGraphVertexModel.getTokens(), Token.TO_WITNESS);
        final StringBuilder label = new StringBuilder();
        for (Witness witness : Ordering.from(Witness.SIGIL_COMPARATOR).sortedCopy(tokens.keySet())) {
          label.append("[").append(witness.getSigil()).append(": '");
          for (Iterator<SimpleToken> tokenIt = Ordering.natural().sortedCopy(Iterables.filter(tokens.get(witness), SimpleToken.class)).iterator(); tokenIt.hasNext(); ) {
            label.append(tokenIt.next().getContent());
            if (tokenIt.hasNext()) {
              label.append(" ");
            }
          }
          label.append("'] ");
        }
        return label.append("(").append(variantGraphVertexModel.getRank()).append(")").toString().trim();
      }
    });
    rc.setEdgeLabelTransformer(new Transformer<VariantGraphEdgeModel, String>() {
      @Override
      public String transform(VariantGraphEdgeModel variantGraphEdgeModel) {
        return Joiner.on(", ").join(Iterables.transform(variantGraphEdgeModel.getWitnesses(), new Function<Witness, String>() {

          @Override
          public String apply(Witness input) {
            return input.getSigil();
          }
        }));
      }
    });
    rc.setVertexFillPaintTransformer(new Transformer<VariantGraphVertexModel, Paint>() {
      @Override
      public Paint transform(VariantGraphVertexModel v) {
        return v.getTokens().isEmpty() ? Color.BLACK : Color.WHITE;
      }
    });
    rc.setEdgeStrokeTransformer(new Transformer<VariantGraphEdgeModel, Stroke>() {
      @Override
      public Stroke transform(VariantGraphEdgeModel variantGraphEdgeModel) {
        return variantGraphEdgeModel.getWitnesses().isEmpty() ? CollateXLaboratory.DASHED_STROKE : CollateXLaboratory.SOLID_STROKE;
      }
    });
  }
}
