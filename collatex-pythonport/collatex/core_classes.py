"""
Created on Apr 19, 2014

@author: Ronald Haentjens Dekker

This module defines the core collation concepts of CollateX

Tokenizer, Witness, VariantGraph, CollationAlgorithm
"""
import json
import networkx as nx
from _collections import deque
from networkx.algorithms.dag import topological_sort
import re
from prettytable import PrettyTable
from textwrap import fill
from collatex.exceptions import TokenError


class Collation(object):

    @classmethod
    def create_from_dict(cls, data, limit=None):
        witnesses = data["witnesses"]
        collation = Collation()
        for witness in witnesses[:limit]:
            # generate collation object from json_data
            collation.add_witness(witness)
        return collation

    @classmethod
    # json_data can be a string or a file
    def create_from_json(cls, json_data):
        data = json.load(json_data)
        collation = cls.create_from_dict(data)
        return collation

    def __init__(self):
        self.witnesses = []

    def add_witness(self, witnessdata):
        witness = Witness(witnessdata)
        self.witnesses.append(witness)

    def add_plain_witness(self, sigil, content):
        return self.add_witness({'id':sigil, 'content':content})


class Row(object):
    def __init__(self, header):
        self.cells = []
        self.header = header

    def append(self, cell):
        self.cells.append(cell)

    def to_list(self):
        return self.cells

    def to_list_of_strings(self):
        return ["".join([listItem.token_data['t'] for listItem in cell]) if cell else None for cell in self.cells]


class Column(object):
    def __init__(self):
        self.tokens_per_witness = {}
        self.variant = False

    def put(self, sigil, token):
        self.tokens_per_witness[sigil] = token


class AlignmentTable(object):
    def __init__(self, collation, graph=None, layout="horizontal",ranks=None):
        self.collation = collation
        self.graph = graph
        self.layout = layout
        self.columns = []
        self.rows = []
        if graph:
            self._construct_table(ranks)

    def _construct_table(self,ranks):
        if ranks:
            ranking = ranks
        else:
            ranking = VariantGraphRanking.of(self.graph)
        vertices_per_rank = ranking.byRank
        # construct columns        
        for rank in vertices_per_rank:
            column = None
            vertices = vertices_per_rank[rank]
            for vertex in vertices:
                if vertex == self.graph.start or vertex == self.graph.end:
                    continue
                if not column:
                    column = Column()
                    self.columns.append(column)
                # find incoming edges for this vertex and check their labels
                edges = self.graph.in_edges(vertex, data=True)
                for (_, _, attrs) in edges:
                    sigli = attrs["label"]
                    for sigil in sigli.split(", "):
                        token = vertex.tokens[sigil]
                        column.put(sigil, token)
                # set status: is a column variant or invariant
                column.variant = len(vertices) > 1 or len(column.tokens_per_witness) != len(self.collation.witnesses)

        # construct rows
        for witness in self.collation.witnesses:
            sigil = witness.sigil
            # we create a new Row for every witness
            row = Row(sigil)
            self.rows.append(row)
            for column in self.columns:
                # Empty cells are represented with a None
                # The create_table_visualization methods should transform the None in something readable
                #  (like a dash)
                row.append(column.tokens_per_witness.get(sigil, None))

    def __str__(self, *args, **kwargs):
        return str(create_table_visualization(self))


# DISPLAY PART OF THE VARIANT GRAPH IN PLAIN/HTML AND VERTICAL OR HORIZONTAL!
def create_table_visualization(table):
    # create visualization of alignment table
    if table.layout == "vertical":
        prettytable = visualizeTableVertically(table)
    elif table.layout == "horizontal":
        prettytable = visualizeTableHorizontal(table)
    else:
        raise Exception("Unknown table layout: " + table.layout)
    return prettytable


def visualizeTableHorizontal(table):
    # print the table horizontal
    x = PrettyTable()
    x.header = False
    for row in table.rows:
        cells = [row.header]
        t_list = [(token.token_data["t"] for token in cell) if cell else ["-"] for cell in row.cells]
        cells.extend([re.sub('\s+$','',"".join(cell)) for cell in t_list])
        x.add_row(cells)
    # alignment can only be set after the field names are known.
    # since add_row sets the field names, it has to be set after x.add_row(cells)
    x.align = "l"
    return x


def visualizeTableVertically(table):
    # print the table vertically
    x = PrettyTable()
    x.hrules = 1
    for row in table.rows:
        # x.add_column(row.header, [fill(cell.token_data["t"], 20) if cell else "-" for cell in row.cells])
        t_list = [(token.token_data["t"] for token in cell) if cell else ["-"] for cell in row.cells]
        x.add_column(row.header, [fill("".join(item), 20) for item in t_list])
    return x


class WordPunctuationTokenizer(object):
    # tokenizer splits on punctuation or whitespace
    def tokenize(self, contents):
        # whitespace is kept with whatever precedes it
        return re.findall(r'\w+\s*|\W+', contents)


class Token(object):
    # tokendata comes in the dictionary that we use for JSON input.
    def __init__(self, tokendata=None):
        if tokendata is None:
            # We can have empty tokens.
            self.token_string = ""
            self.token_data = {}
        elif 'n' in tokendata:
            self.token_string = tokendata['n']
        elif 't' in tokendata:
            self.token_string = tokendata['t']
        else:
            raise TokenError('No defined token string in tokendata')
        self.token_data = tokendata

    def __repr__(self):
        return self.token_string


class Witness(object):
    def __init__(self, witnessdata):
        self.sigil = witnessdata['id']
        self._tokens = []
        if 'content' in witnessdata:
            self.content = witnessdata['content']
            # print("Witness "+sigil+" TOKENIZER IS CALLED!")
            tokenizer = WordPunctuationTokenizer()
            tokens_as_strings = tokenizer.tokenize(self.content)
            for token_string in tokens_as_strings:
                self._tokens.append(Token({'t': token_string, 'n': re.sub(r'\s+$', '', token_string)}))
        elif 'tokens' in witnessdata:
            for tk in witnessdata['tokens']:
                self._tokens.append(Token(tk))
            # content string is used for generation of the suffix and LCP arrays.
            self.content = ' '.join([x.token_string for x in self._tokens])

    def tokens(self):
        return self._tokens


class VariantGraphVertex(object):
    def __init__(self, token=None, sigil=None):
        self.label = token.token_string if token else ''
        self.tokens = {sigil: [token]} if sigil else {}

    def add_token(self, sigil, token):
        if sigil in self.tokens:
            self.tokens[sigil].append(token)
        else:
            self.tokens[sigil] = [token]

    def __str__(self):
        return self.label if self.label else 'no label'

    def __repr__(self):
        return str(self)


class VariantGraph(object):
    def __init__(self):
        self.graph = nx.DiGraph()
        # Start and end are the only nodes without sigil or tokens
        self.start = self.add_vertex(None, None)
        self.end = self.add_vertex(None, None)

    def add_vertex(self, token, sigil):
        newVertex = VariantGraphVertex(token, sigil)
        self.graph.add_node(newVertex)
        # Returned to aligner, which tracks relationship of tokens and vertices
        return newVertex

    def connect(self, source, target, witnesses):
        """
        :type source: integer
        :type target: integer
        """
        # print("Adding Edge: "+source+":"+target)
        if self.graph.has_edge(source, target):
            self.graph[source][target]["label"] += ", " + str(witnesses)
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
        # return self.graph.get_edge_data(node, node2)
        return self.graph.has_edge(node, node2)

    def in_edges(self, node, data=False):
        return self.graph.in_edges(nbunch=node, data=data)

    def out_edges(self, node, data=False):
        return self.graph.out_edges(nbunch=node, data=data)

    def vertex_attributes(self, node):
        return self.graph.node[node]

    # Note: generator implementation
    def vertexWith(self, content):
        try:
            vertex_to_find = next(n for n in self.vertices() if n.label == content)
            return vertex_to_find
        except StopIteration:
            raise Exception("Vertex with " + content + " not found!")


class CollationAlgorithm(object):
    def merge(self, graph, witness_sigil, witness_tokens, alignments={}):
        """
        :type graph: VariantGraph
        """
        # NOTE: token_to_vertex only contains newly generated vertices
        token_to_vertex = {}
        last = graph.start
        for token in witness_tokens:
            vertex = alignments.get(token, None)
            if not vertex:
                vertex = graph.add_vertex(token, witness_sigil)
                token_to_vertex[token] = vertex
            else:
                vertex.add_token(witness_sigil, token)
                # graph.add_token_to_vertex(vertex, token, witness_sigil)
            graph.connect(last, vertex, witness_sigil)
            last = vertex
        graph.connect(last, graph.end, witness_sigil)
        return token_to_vertex


# TODO: define abstract collation class


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
            can_join = join_candidate != end and len(graph.in_edges(join_candidate)) == 1
            if can_join:
                join_vertex_and_join_candidate(graph, join_candidate, vertex)
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
            if neighbor not in processed:
                queue.appendleft(neighbor)


def join_vertex_and_join_candidate(graph, join_candidate, vertex):
    # Note: since there is no normalized/non normalized content in the graph
    # a space character is added here for non punctuation tokens

    if re.match(r'^\W', join_candidate.label):
        vertex.label += join_candidate.label
    else:
        vertex.label += (" " + join_candidate.label)
    # join_candidate must have exactly one token (inside a list); left item may have more
    for siglum, token in join_candidate.tokens.items():
        vertex.add_token(siglum, token[0])


# Port of VariantGraphRanking class from Java
# This is a minimal port; only bare bones
class VariantGraphRanking(object):
    # Do not class the constructor, use the of class method instead!
    def __init__(self):
        # Note: A vertex can have only one rank
        # however, a rank can be assigned to multiple vertices
        self.byVertex = {}
        self.byRank = {}

    def apply(self, vertex):
        return self.byVertex[vertex]

    @classmethod
    def of(cls, graph):
        variant_graph_ranking = VariantGraphRanking()
        topological_sorted_vertices = topological_sort(graph.graph)
        for v in topological_sorted_vertices:
            rank = -1
            for (source, _) in graph.in_edges(v):
                rank = max(rank, variant_graph_ranking.byVertex[source])
            rank += 1
            variant_graph_ranking.byVertex[v] = rank
            variant_graph_ranking.byRank.setdefault(rank, []).append(v)
        return variant_graph_ranking
