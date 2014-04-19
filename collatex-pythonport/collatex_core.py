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
        token_to_vertex = {}
        last = graph.start
        for token in witness_tokens:
            vertex = alignments.get(token, None)
            if vertex == None:
                vertex = graph.add_vertex(token)
            else:
                #TODO: add Exception(msg)
                raise("we need to add a token to a vertex, but we don't know how yet!")
                #vertex.add_token
            token_to_vertex[token] = vertex
            #TODO: add witness set!
            graph.connect(last, vertex)
            last = vertex
        graph.connect(last, graph.end)
        return token_to_vertex

#TODO: define abstract collation class


    
    




