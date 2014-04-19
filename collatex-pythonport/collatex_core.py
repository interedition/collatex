'''
Created on Apr 19, 2014

@author: Ronald Haentjens Dekker

This module defines the core collation concepts of CollateX

Tokenizer, Witness, VariantGraph, CollationAlgorithm
'''
import networkx as nx

# not used in the suffix implementation
# Tokenizer inside suffix array library is used
class Tokenizer(object):
    
    #by default the tokenizer splits on space characters    
    def tokenize(self, contents):
        return contents.split()

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

#TODO: define abstract collation class


    
    



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

