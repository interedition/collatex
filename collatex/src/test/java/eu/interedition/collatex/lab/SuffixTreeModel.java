package eu.interedition.collatex.lab;

import com.google.common.collect.Lists;
import edu.uci.ics.jung.graph.DelegateTree;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.suffixtree.SuffixTree;
import eu.interedition.collatex.suffixtree.SuffixTreeNode;

import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SuffixTreeModel extends DelegateTree<SuffixTreeVertexModel, SuffixTreeEdgeModel> {

  public SuffixTreeModel(SuffixTree<Token> suffixTree) {
    add(suffixTree, null, suffixTree.getRoot());
  }

  void add(SuffixTree<Token> st, SuffixTreeVertexModel parent, SuffixTreeNode node) {
    final int start = node.getEdgeLabelStart();
    final int end = st.getNodeLabelEnd(node);

    final List<Token> tokens = Lists.newArrayListWithExpectedSize(end - start);
    for (int offset = start; offset <= end; offset++) {
      tokens.add(st.getSource()[offset]);
    }

    final SuffixTreeVertexModel vertexModel = new SuffixTreeVertexModel(tokens, node.getPathPosition());
    if (parent == null) {
      setRoot(vertexModel);
    } else {
      addChild(new SuffixTreeEdgeModel(), parent, vertexModel);
    }

    for (SuffixTreeNode child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
      add(st, vertexModel, child);
    }
  }

}
