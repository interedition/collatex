'''
Created on Apr 10, 2014

@author: Ronald Haentjens Dekker
'''

import unittest
import networkx as nx
from collatex_simple import Tokenizer, SuperMaximumRe, Block
from linsuffarr import SuffixArray
from ClusterShell.RangeSet import RangeSet

class Witness(object):
    
    def __init__(self, sigil, content):
        self.sigil = sigil
        self.content = content

    def tokens(self):
        tokenizer = Tokenizer()
        return tokenizer.tokenize(self.content)

class VariantGraph(object):
    
    def __init__(self):
        self.graph = nx.Graph()
        # hmm they are too similar now
        self.start = self.graph.add_node("")
        self.end = self.graph.add_node("")
        
        
    def vertices(self):
        return self.graph.nodes_iter()
    
    def edge_between(self, node, node2):
        #return self.graph.get_edge_data(node, node2)
        return self.graph.has_edge(node, node2)
  
  
class CollationAlgorithm(object):
    def merge(self, graph, witness_tokens, alignments = {}):  
        last = graph.start
        for token in witness_tokens:
            vertex = alignments[token]
            if (vertex == None):
                graph.add_vertex(token)
            else:
                #TODO: add Exception(msg)
                raise("we need to add a token to a vertex, but we don't know how yet!")
                #vertex.add_token
        # make new edge and connect the last vertex and the new vertex
        
#     protected void merge(VariantGraph into, Iterable<Token> witnessTokens, Map<Token, VariantGraph.Vertex> alignments) {
#       Preconditions.checkArgument(!Iterables.isEmpty(witnessTokens), "Empty witness");
#       final Witness witness = Iterables.getFirst(witnessTokens, null).getWitness();
# 
#       if (LOG.isLoggable(Level.FINE)) {
#         LOG.log(Level.FINE, "{0} + {1}: Merge comparand into graph", new Object[] { into, witness });
#       }
#       witnessTokenVertices = Maps.newHashMap();
#       VariantGraph.Vertex last = into.getStart();
#       final Set<Witness> witnessSet = Collections.singleton(witness);
#       for (Token token : witnessTokens) {
#         VariantGraph.Vertex matchingVertex = alignments.get(token);
#         if (matchingVertex == null) {
#           matchingVertex = into.add(token);
#         } else {
#           if (LOG.isLoggable(Level.FINE)) {
#             LOG.log(Level.FINE, "Match: {0} to {1}", new Object[] { matchingVertex, token });
#           }
#           matchingVertex.add(Collections.singleton(token));
#         }
#         witnessTokenVertices.put(token, matchingVertex);
# 
#         into.connect(last, matchingVertex, witnessSet);
#         last = matchingVertex;
#       }
#       into.connect(last, into.getEnd(), witnessSet);
#     }



class Collation(object):
    witnesses = []
    counter = 0
    witness_ranges = {}
    combined_string = ""
    
    # the tokenization process happens multiple times
    # and by different tokenizers. This should be fixed
    def add_witness(self, sigil, content):
        witness = Witness(sigil, content)
        self.witnesses.append(witness)
        witness_range = range(self.counter, self.counter+len(witness.tokens()))
        # the extra one is for the marker token
        self.counter += len(witness.tokens()) +1 
        self.witness_ranges[sigil] = witness_range
        if not self.combined_string == "":
            self.combined_string += " $ "
        self.combined_string += content
        
    def get_blocks(self):
        sa = SuffixArray(self.combined_string)
        smr = SuperMaximumRe()
        blocks = smr.find_blocks(sa)
        return blocks
    
    def collate(self):
        self.graph = VariantGraph() 
        return self.graph

    def get_range_for_witness(self, witness_sigil):
        if not self.witness_ranges.has_key(witness_sigil):
            raise Exception("Witness "+witness_sigil+" is not added to the collation!")
        return self.witness_ranges[witness_sigil]
    
    def get_combined_string(self):
        return self.combined_string



# ugly
# move to variant graph class
def vertexWith(graph, content):
    vertex_to_find = None
    for vertex in graph.vertices():
        print(type(vertex))
        if vertex == content:
            vertex_to_find = vertex
            break
    return vertex_to_find    

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
        self.assertEquals("a b c d F g h i ! K ! q r s t $ a b c d F g h i ! q r s t", collation.get_combined_string())
        blocks = collation.get_blocks()
        # we expect two blocks ("a b c d F g h i !", "q r s t")
        # both numbers are inclusive
        block1 = Block(RangeSet("0-8, 16-24"))
        block2 = Block(RangeSet("11-14, 25-28"))
        #print(blocks)
        self.assertEqual([block1, block2], blocks)

    
    
    
    
    
    
    
    
    
    
    
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
