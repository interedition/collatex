'''
Created on May 3, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from networkx.drawing.nx_pydot import to_pydot
from collatex.core_classes import VariantGraph
from collatex import Collation
#from collatex.dekker_suffix_algorithm import DekkerSuffixAlgorithm


class Test(unittest.TestCase):

    #TODO: this test is not finished! 
    # def test_Hermans_case_variantgraph(self):
    #     collation = Collation()
    #     collation.add_plain_witness("W1", "a b c d F g h i ! K ! q r s t")
    #     collation.add_plain_witness("W2", "a b c d F g h i ! q r s t")
    #     graph = VariantGraph()
    #     algorithm = DekkerSuffixAlgorithm(collation)
    #     algorithm.build_variant_graph_from_blocks(graph, collation)
    #     start_vertex = graph.start
    #     a = graph.vertexWith("a")
    #     b = graph.vertexWith("b")
    #     t = graph.vertexWith("t")
    #     end_vertex = graph.end
    #     self.assert_(graph.edge_between(start_vertex, a))
    #     self.assert_(graph.edge_between(a, b))
    #     self.assert_(graph.edge_between(t, end_vertex))
    #     #print(len(graph.vertices()))
    #     #print(len(graph.edges()))
    #
    #     # display graph
    #     #print(graph.graph.nodes())
    #     #view_pygraphviz(graph.graph)
    #
#         dot = to_pydot(graph.graph)
#         dot.write("rawoutput")

#     def test_variant_graph_two_equal_witnesses(self):
#         collation = Collation()
#         collation.add_plain_witness('A', 'the black cat')
#         collation.add_plain_witness('B', 'the black cat')
#         graph = collation.collate()
#         the_vertex = vertexWith(graph, 'the')
#         black_vertex = vertexWith(graph, 'black')
#         cat_vertex = vertexWith(graph, 'cat')
#         
#         self.assert_(graph.edge_between(graph.start, the_vertex))
#         self.assert_(graph.edge_between(the_vertex, black_vertex))
#         self.assert_(graph.edge_between(cat_vertex, graph.end))
#       
#         @Test
#   public void twoWitnesses() {
#     final SimpleWitness[] w = createWitnesses("the black cat", "the black cat");
#     final VariantGraph graph = collate(w);
# 
#     assertEquals(5, Iterables.size(graph.vertices()));
#     assertEquals(4, Iterables.size(graph.edges()));
# 
#     final VariantGraph.Vertex theVertex = vertexWith(graph, "the", w[0]);
#     final VariantGraph.Vertex blackVertex = vertexWith(graph, "black", w[0]);
#     final VariantGraph.Vertex catVertex = vertexWith(graph, "cat", w[0]);
# 
#     assertHasWitnesses(edgeBetween(graph.getStart(), theVertex), w[0], w[1]);
#     assertHasWitnesses(edgeBetween(theVertex, blackVertex), w[0], w[1]);
#     assertHasWitnesses(edgeBetween(blackVertex, catVertex), w[0], w[1]);
#     assertHasWitnesses(edgeBetween(catVertex, graph.getEnd()), w[0], w[1]);
    pass

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()