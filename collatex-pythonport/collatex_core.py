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
        #TODO: make it a DiGraph
        self.graph = nx.Graph()
        self.start = self.add_vertex("")
        self.end = self.add_vertex("")
    
    # vertex creation uses a unique ID, since the token_content does not have to be unique   
    # we store the token content in the label 
    def add_vertex(self, token_content):
        '''
        :type token_content: string
        '''
        node_id = self.graph.number_of_nodes()
        #print("Adding node: "+node_id+":"+token_content)
        self.graph.add_node(node_id, label= token_content)
        return node_id
    
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
  
    # Note: generator implementation
    def vertexWith(self, content):
        try:
            vertex_to_find = (n for n in self.graph if self.graph.node[n]['label']==content).next()
            return vertex_to_find    
        except StopIteration:
            raise Exception("Vertex with "+content+" not found!")    
  
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
            #TODO: add vertex.add(token)
            #else:
            #    raise Exception("we need to add a token to a vertex, but we don't know how yet!")
            token_to_vertex[token] = vertex
            #TODO: add witness set!
            graph.connect(last, vertex)
            last = vertex
        graph.connect(last, graph.end)
        return token_to_vertex

#TODO: define abstract collation class


    
    




