'''
Created on Apr 10, 2014

@author: Ronald Haentjens Dekker
'''

import unittest
from ClusterShell.RangeSet import RangeSet
from collatex_suffix import Collation, Block


class Test(unittest.TestCase):
    '''
    classdocs
    '''
    # test whether the witness->range mapping works
    def test_Hermans_case_witness(self):
        collation = Collation()
        collation.add_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_witness("W2", "a b c d F g h i ! q r s t")
        self.assertEquals(range(0, 15), collation.get_range_for_witness("W1"))
        self.assertEquals(range(16, 29), collation.get_range_for_witness("W2"))

    def test_Hermans_case_blocks(self):
        collation = Collation()
        collation.add_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_witness("W2", "a b c d F g h i ! q r s t")
        # $ is meant to separate witnesses here
        self.assertEquals("a b c d F g h i ! K ! q r s t $1 a b c d F g h i ! q r s t", collation.get_combined_string())
        blocks = collation.get_blocks()
        # we expect two blocks ("a b c d F g h i !", "q r s t")
        # both numbers are inclusive
        block1 = Block(RangeSet("0-8, 16-24"))
        block2 = Block(RangeSet("11-14, 25-28"))
        #print(blocks)
        self.assertEqual([block1, block2], blocks)
        
    def test_Hermans_case_blocks_three_witnesses(self):
        collation = Collation()
        collation.add_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_witness("W2", "a b c d F g h i ! q r s t")
        collation.add_witness("W3", "a b c d E g h i ! q r s t")
        sa = collation.get_sa()
        print(sa)
        blocks = collation.get_blocks()
        print(blocks)
#         # we expect two blocks ("a b c d F g h i !", "q r s t")
#         # both numbers are inclusive
#         block1 = Block(RangeSet("0-8, 16-24"))
#         block2 = Block(RangeSet("11-14, 25-28"))
#         self.assertEqual([block1, block2], blocks)
    
    
    
    
    
    
    
    
    
    
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
