'''
Created on Apr 10, 2014

@author: Ronald Haentjens Dekker
'''

import unittest
from ClusterShell.RangeSet import RangeSet
from collatex_suffix import Collation, Block, DekkerSuffixAlgorithmn
from collatex_core import VariantGraph
from networkx.drawing.nx_pydot import to_pydot


class Test(unittest.TestCase):
    # test whether the witness->range mapping works
    def test_Hermans_case_witness(self):
        collation = Collation()
        collation.add_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_witness("W2", "a b c d F g h i ! q r s t")
        self.assertEquals(range(0, 15), collation.get_range_for_witness("W1"))
        self.assertEquals(range(16, 29), collation.get_range_for_witness("W2"))

    def test_Hermans_non_overlapping_blocks(self):
        collation = Collation()
        collation.add_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_witness("W2", "a b c d F g h i ! q r s t")
        repeats = collation.get_non_overlapping_repeating_blocks()
        print(repeats)
        self.fail("TESTING")
    
    def test_black_cat_non_overlapping_blocks(self):
        collation = Collation()
        collation.add_witness("W1", "the black cat")
        collation.add_witness("W2", "the black cat")
        repeats = collation.get_non_overlapping_repeating_blocks()
        print(repeats)
        self.fail("TESTING")

    def test_Hermans_case_blocks_three_witnesses(self):
        collation = Collation()
        collation.add_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_witness("W2", "a b c d F g h i ! q r s t")
        collation.add_witness("W3", "a b c d E g h i ! q r s t")
        repeats = collation.get_non_overlapping_repeating_blocks()
        print(repeats)
        self.fail("TESTING")

    
    
    def test_Hermans_case_blocks(self):
        collation = Collation()
        collation.add_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_witness("W2", "a b c d F g h i ! q r s t")
        # $ is meant to separate witnesses here
        # TODO: Re-enable this later! Tests at the moments returns $3
        # self.assertEquals("a b c d F g h i ! K ! q r s t $1 a b c d F g h i ! q r s t", collation.get_combined_string())
        blocks = collation.get_blocks()
        # we expect two blocks ("a b c d F g h i !", "q r s t")
        # both numbers are inclusive
        block1 = Block(RangeSet("0-8, 16-24"))
        block2 = Block(RangeSet("11-14, 25-28"))
        #print(blocks)
        print(collation.get_lcp_array())
        self.assertEqual([block1, block2], blocks)
     
    #TODO: this test is not finished! 
    def test_Hermans_case_variantgraph(self):
        collation = Collation()
        collation.add_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_witness("W2", "a b c d F g h i ! q r s t")
        graph = VariantGraph()
        algorithm = DekkerSuffixAlgorithmn()
        algorithm.buildVariantGraphFromBlocks(graph, collation)
        start_vertex = graph.start
        a = graph.vertexWith("a")
        b = graph.vertexWith("b")
        t = graph.vertexWith("t")
        end_vertex = graph.end
        self.assert_(graph.edge_between(start_vertex, a))
        self.assert_(graph.edge_between(a, b))
        self.assert_(graph.edge_between(t, end_vertex))
        #print(len(graph.vertices()))
        #print(len(graph.edges()))
        
        # display graph
        #print(graph.graph.nodes())
        #view_pygraphviz(graph.graph)
        
        dot = to_pydot(graph.graph)
        dot.write("rawoutput")
  
        pass
         
            
#     def test_Hermans_case_blocks_three_witnesses(self):
#         collation = Collation()
#         collation.add_witness("W1", "a b c d F g h i ! K ! q r s t")
#         collation.add_witness("W2", "a b c d F g h i ! q r s t")
#         collation.add_witness("W3", "a b c d E g h i ! q r s t")
#         sa = collation.get_sa()
#         print(sa)
#         blocks = collation.get_blocks()
#         print(blocks)
# #         # we expect two blocks ("a b c d F g h i !", "q r s t")
# #         # both numbers are inclusive
# #         block1 = Block(RangeSet("0-8, 16-24"))
# #         block2 = Block(RangeSet("11-14, 25-28"))
# #         self.assertEqual([block1, block2], blocks)
    
    
    
    
    
    
    
    
    
    
#     def test_variant_graph_two_equal_witnesses(self):
#         collation = Collation()
#         collation.add_witness('A', 'the black cat')
#         collation.add_witness('B', 'the black cat')
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

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()