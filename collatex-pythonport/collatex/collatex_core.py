'''
Created on Apr 19, 2014

@author: Ronald Haentjens Dekker

This module defines the core collation concepts of CollateX

Tokenizer, Witness, VariantGraph, CollationAlgorithm
'''
import networkx as nx
from _collections import deque

# not used in the suffix implementation
# Tokenizer inside suffix array library is used
class Tokenizer(object):
    
    #by default the tokenizer splits on space characters    
    def tokenize(self, contents):
        return contents.split()


class Token(object):
    
    def __init__(self, tokenstring):
        self.token_string = tokenstring

    def __repr__(self):
        return self.token_string

class Witness(object):
    
    def __init__(self, sigil, content):
        self.sigil = sigil
        self.content = content
        tokenizer = Tokenizer()
        self._tokens = []
        tokens_as_strings = tokenizer.tokenize(self.content)
        for token_string in tokens_as_strings:
            self._tokens.append(Token(token_string))
            
    def tokens(self):
        return self._tokens

class VariantGraph(object):
    
    def __init__(self):
        self.graph = nx.DiGraph()
        self.start = self.add_vertex(Token(""))
        self.end = self.add_vertex(Token(""))
    
    # vertex creation uses a unique ID, since the token_content does not have to be unique   
    # we store the token content in the label 
    def add_vertex(self, token):
        '''
        :type token: Token
        '''
        node_id = self.graph.number_of_nodes()
        #print("Adding node: "+node_id+":"+token_content)
        self.graph.add_node(node_id, label= token.token_string)
        return node_id
    
    def connect(self, source, target, witnesses):
        """
        :type source: integer
        :type target: integer
        """
        #print("Adding Edge: "+source+":"+target)
        if self.graph.has_edge(source, target):
            self.graph[source][target]["label"]+=", "+str(witnesses)
        else:    
            self.graph.add_edge(source, target, label=witnesses)
        
    def remove_edge(self, source, target):
        self.graph.remove_edge(source, target)
        
    def remove_node(self, node):
        self.graph.remove_node(node)      
        
    def vertices(self):
        return self.graph.nodes()
    
    def edges(self):
        return self.graph.edges()
    
    def edge_between(self, node, node2):
        #return self.graph.get_edge_data(node, node2)
        return self.graph.has_edge(node, node2)
    
    def in_edges(self, node):
        return self.graph.in_edges(nbunch=node)
    
    def out_edges(self, node, data=False):
        return self.graph.out_edges(nbunch=node, data=data)
    
    def vertex_attributes(self, node):
        return self.graph.node[node]
  
    # Note: generator implementation
    def vertexWith(self, content):
        try:
            vertex_to_find = (n for n in self.graph if self.graph.node[n]['label']==content).next()
            return vertex_to_find    
        except StopIteration:
            raise Exception("Vertex with "+content+" not found!")    
  
class CollationAlgorithm(object):
    def merge(self, graph, witness_sigil, witness_tokens, alignments = {}):  
        """
        :type graph: VariantGraph
        """
        #NOTE: token_to_vertex only contains newly generated vertices
        token_to_vertex = {}
        last = graph.start
        for token in witness_tokens:
            vertex = alignments.get(token, None)
            if vertex == None:
                vertex = graph.add_vertex(token)
                token_to_vertex[token] = vertex
            #TODO: add token to vertex, for example: vertex.add(token)
            graph.connect(last, vertex, witness_sigil)
            last = vertex
        graph.connect(last, graph.end, witness_sigil)
        return token_to_vertex

#TODO: define abstract collation class


'''
 This function joins the variant graph in place.
 This function is a straight port of the Java version of CollateX.
    :type graph: VariantGraph
 TODO: add transposition support!   
'''
def join(graph):
    processed = set()
    end = graph.end
    queue = deque()
    for (_, neighbor) in graph.out_edges(graph.start):
        queue.appendleft(neighbor)
    while queue:
        vertex = queue.popleft()
        out_edges = graph.out_edges(vertex)
        if len(out_edges) is 1:
            (_, join_candidate) = out_edges[0]
            can_join = join_candidate != end and len(graph.in_edges(join_candidate))==1
            if can_join:
                graph.vertex_attributes(vertex)["label"]+=" "+graph.vertex_attributes(join_candidate)["label"]
                for (_, neighbor, data) in graph.out_edges(join_candidate, data=True):
                    graph.remove_edge(join_candidate, neighbor)
                    graph.connect(vertex, neighbor, data["label"])
                graph.remove_edge(vertex, join_candidate)
                graph.remove_node(join_candidate) 
                queue.appendleft(vertex);
                continue;
        processed.add(vertex)
        for (_, neighbor) in out_edges:
            # FIXME: Why do we run out of memory in some cases here, if this is not checked?
            if not neighbor in processed:
                queue.appendleft(neighbor)
                
# Port of VariantGraphRanking class from Java
# This is a minimal port; only bare bones
class VariantGraphRanking(object):
    # Do not class the constructor, use the of class method instead!
    def __init__(self):
        #Note: A vertex can have only one rank
        #however, a rank can be assigned to multiple vertices
        self.byVertex = {}
        self.byRank = {}
    
    @classmethod
    def of(cls, graph):
        variant_graph_ranking = VariantGraphRanking(graph)                
        for v in graph.vertices():
            rank = -1
            for (source, _) in graph.in_edges(v):
                rank = max(rank, variant_graph_ranking.byVertex[source])
            rank += 1
            variant_graph_ranking.byVertex[v]=rank
            variant_graph_ranking.byRank.setdefault(rank, []).append(v)
        return variant_graph_ranking

    
    




