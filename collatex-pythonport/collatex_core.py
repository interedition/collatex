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
        self.start = self.add_vertex("")
        self.end = self.add_vertex("")
        
    def add_vertex(self, token_content):
        '''
        :type token_content: string
        '''
        #print("Adding node: "+token_content)
        self.graph.add_node(token_content)
        return token_content
    
    def connect(self, source, target):
        """
        :type source: vertex
        :type target: vertex
        """
        #print("Adding Edge: "+source+":"+target)
        self.graph.add_edge(source, target)
        
    def vertices(self):
        return self.graph.nodes()
    
    def edges(self):
        return self.graph.edges()
    
    def edge_between(self, node, node2):
        #return self.graph.get_edge_data(node, node2)
        return self.graph.has_edge(node, node2)
  
    # ugly implementation
    def vertexWith(self, content):
        vertex_to_find = None
        for vertex in self.vertices():
            if vertex == content:
                vertex_to_find = vertex
                break
        if vertex_to_find == None:
            raise Exception("Vertex with "+content+" not found!")    
        return vertex_to_find    
  
class CollationAlgorithm(object):
    def merge(self, graph, witness_tokens, alignments = {}):  
        """
        :type graph: VariantGraph
        """
        last = graph.start
        for token in witness_tokens:
            vertex = alignments.get(token, None)
            if (vertex == None):
                vertex = graph.add_vertex(token)
            else:
                #TODO: add Exception(msg)
                raise("we need to add a token to a vertex, but we don't know how yet!")
                #vertex.add_token
            # make new edge and connect the last vertex and the new vertex
            #TODO: add witness set!
            graph.connect(last, vertex)
            last = vertex
        graph.connect(last, graph.end)

        
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


    
    




