'''
Created on Aug 5, 2014

@author: Ronald Haentjens Dekker
'''

from collatex.collatex_core import CollationAlgorithm
from sets import Set
from collatex.suffix_based_scorer import Scorer
from prettytable import PrettyTable
from collatex.astar import AStarNode, AStar

class AstarEditGraphNode(AStarNode):
    def __init__(self, aligner, y, x):
        self.aligner = aligner
        self.y = y
        self.x = x
        self.match = False
        self.segments = 0 #TODO: remove
        super(AstarEditGraphNode, self).__init__()

    def __repr__(self):
        return repr(self.g)
#         return str(self.y)+" "+str(self.x)+" "+
    
    def is_end_node(self):
        is_end = self.y == self.aligner.length_witness_b and self.x == self.aligner.length_witness_a
#         print("Node: "+str(self.y)+" "+str(self.x)+" "+str(self.edit_operation)+" is end: "+str(is_end))
        return is_end 
    
    # TODO: not nice: scorer already updates global score of other..
    def move_cost(self, other):
        #NOTE: possible optimization: you don't always need to fetch the tokens!
        token_a = self.aligner.tokens_witness_a[other.x-1]
        token_b = self.aligner.tokens_witness_b[other.y-1]
        edit_operation = 1
        if other.x -1 == self.x and other.y - 1 == self.y:
            edit_operation = 0
        #     def score_cell(self, table_node, parent_node, token_a, token_b, y, x, edit_operation):
        self.aligner.scorer.score_cell(other, self, token_a, token_b, other.y, other.x, edit_operation)
        cost = other.g - self.g
#         print("From Node: "+str(self.y)+" "+str(self.x)+" "+str(self.edit_operation)+" to other: "+str(other.y)+" "+str(other.x)+" "+str(other.edit_operation)+" cost: "+str(cost))
        return -cost

class AstarEditGraphAligner(AStar):
    def __init__(self, tokens_witness_a, tokens_witness_b, scorer):
        self.tokens_witness_a = tokens_witness_a
        self.tokens_witness_b = tokens_witness_b
        self.scorer = scorer
        self.table = None #TODO: not nice!
        self.length_witness_a = len(self.tokens_witness_a)
        self.length_witness_b = len(self.tokens_witness_b)

    def create_childnodes(self, node):
        # we create 3 child nodes
        # NOTE: In some cases it is possible to get away with only 2 nodes
        # NOTE: possible performance enhancement
        child_nodes = []
        if node.y < self.length_witness_b:
            child_node = self.table[node.y+1][node.x]
            child_nodes.append(child_node)
        if node.x < self.length_witness_a:
            child_node = self.table[node.y][node.x+1]
            child_nodes.append(child_node)
        if node.y < self.length_witness_b and node.x < self.length_witness_a:
            child_node = self.table[node.y+1][node.x+1]
            child_nodes.append(child_node)
        return child_nodes

    def heuristic(self, node):
        distance_to_end_node_horizontal = (self.length_witness_a-node.x)
        distance_to_end_node_vertical = (self.length_witness_b-node.y)
        gap_penalty = abs(distance_to_end_node_horizontal - distance_to_end_node_vertical)
#         print("heuristic penalty: "+str(node.y)+" "+str(node.x)+" penalty: "+str(-gap_penalty))
        return gap_penalty

class EditGraphNode(object):
    def __init__(self):
        self.g = 0 # global score 
        self.segments = 0 # number of segments
        self.match = False # this node represents a match or not
        
    def __repr__(self):
        return repr(self.g)

    '''
    Aligner based on an edit graph.
    It needs a g function and a definition of a match.
    Since every node of the graph has three children the graph is represented as a table internally.
    Default implementation is a* based.
    '''
class EditGraphAligner(CollationAlgorithm):
    def __init__(self, collation, near_match=False, astar=False, debug_scores=False):
        self.collation = collation
        self.debug_scores = debug_scores
        self.scorer = Scorer(collation, near_match)
        if not astar:
            self.align_function = self._align_table
        else:
            print("INFO: Aligning using a* search algorithm. BETA quality.")
            self.align_function = self._align_astar
            
    def collate(self, graph, collation):
        '''
        :type graph: VariantGraph
        :type collation: Collation
        '''
        # Build the variant graph for the first witness
        # this is easy: generate a vertex for every token
        first_witness = collation.witnesses[0]
        tokens = first_witness.tokens()
        token_to_vertex = self.merge(graph, first_witness.sigil, tokens)
        
        # let the scorer prepare the first witness
        self.scorer.prepare_witness(first_witness)
        
        # construct superbase
        superbase = tokens
        
        # align witness 2 - n
        for x in range(1, len(collation.witnesses)):
            next_witness = collation.witnesses[x]
        
            # let the scorer prepare the next witness
            self.scorer.prepare_witness(next_witness)
            
#             # VOOR CONTROLE!
#             alignment = self._align_table(superbase, next_witness, token_to_vertex)
#             self.table2 = self.table
            
            # alignment = token -> vertex
            alignment = self.align_function(superbase, next_witness, token_to_vertex)
        
            # merge
            token_to_vertex.update(self.merge(graph, next_witness.sigil, next_witness.tokens(), alignment))

#             print("actual")
#             self._debug_edit_graph_table(self.table)
#             print("expected")
#             self._debug_edit_graph_table(self.table2)
            
            # change superbase
            superbase = self.new_superbase
        
        if self.debug_scores:
            self._debug_edit_graph_table(self.table)
        
    def _align_astar(self, superbase, witness, token_to_vertex, control_table=None):
        self.tokens_witness_a = superbase
        self.tokens_witness_b = witness.tokens()
        self.length_witness_a = len(self.tokens_witness_a)
        self.length_witness_b = len(self.tokens_witness_b)
        self.control_table = control_table
        aligner = AstarEditGraphAligner(self.tokens_witness_a, self.tokens_witness_b, self.scorer)
        self.table = [[AstarEditGraphNode(aligner, y, x) for x in range(self.length_witness_a+1)] for y in range(self.length_witness_b+1)]
        aligner.table = self.table
        start = self.table[0][0]
        path = aligner.search(start, self.control_table)
        
        # transform path into an alignment
        alignment = {}

        # segment stuff
        # note we traverse from left to right!
        self.last_x = 0
        self.last_y = 0
        self.new_superbase=[]
        
        for element in path:
#             print(element.y, element.x)
            
            if element.match == True:
                # process segments
                self.newer_add_to_superbase(self.tokens_witness_a, self.tokens_witness_b, element.x, element.y)
                self.last_x = element.x
                self.last_y = element.y
                # process alignment
                token = self.tokens_witness_a[element.x-1]
                token2 = self.tokens_witness_b[element.y-1]
                vertex = token_to_vertex[token]
                alignment[token2] = vertex
                # add match to superbase
                self.new_superbase.append(token)

        # process additions/omissions in the begin of the superbase/witness
        self.newer_add_to_superbase(self.tokens_witness_a, self.tokens_witness_b, self.length_witness_a, self.length_witness_b)
        return alignment
    
    
    def newer_add_to_superbase(self, witness_a, witness_b, x, y):
        if x - self.last_x - 1 > 0 or y - self.last_y - 1 > 0:
            # create new segment
            omitted_base = witness_a[self.last_x:x - 1]
            added_witness = witness_b[self.last_y:y - 1]
            self.new_superbase += omitted_base
            self.new_superbase += added_witness
    
    def _align_table(self, superbase, witness, token_to_vertex):
        self.tokens_witness_a = superbase
        self.tokens_witness_b = witness.tokens()
        self.length_witness_a = len(self.tokens_witness_a)
        self.length_witness_b = len(self.tokens_witness_b)
        self.table = [[EditGraphNode() for _ in range(self.length_witness_a+1)] for _ in range(self.length_witness_b+1)]

        # per diagonal calculate the score (taking into account the three surrounding nodes)
        self.traverse_diagonally()

        alignment = {}

        # segment stuff
        # note we traverse from right to left!
        self.last_x = self.length_witness_a
        self.last_y = self.length_witness_b
        self.new_superbase=[]
        
        # start lower right cell
        x = self.length_witness_a
        y = self.length_witness_b
        # work our way to the upper left
        while x > 0 and y > 0:
            self._process_cell(token_to_vertex, self.tokens_witness_a, self.tokens_witness_b, alignment, x, y)
            # examine neighbor nodes
            nodes_to_examine = Set()
            nodes_to_examine.add(self.table[y][x-1])
            nodes_to_examine.add(self.table[y-1][x])
            nodes_to_examine.add(self.table[y-1][x-1])
            # calculate the maximum scoring parent node
            parent_node = max(nodes_to_examine, key=lambda x: x.g)
            # move position
            if self.table[y-1][x-1] == parent_node:
                # another match or replacement
                y = y -1
                x = x -1
            else:
                if self.table[y-1][x] == parent_node:
                    #omission?
                    y = y -1
                else:
                    if self.table[y][x-1] == parent_node:
                        #addition?
                        x = x -1
        # process additions/omissions in the begin of the superbase/witness
        self.add_to_superbase(self.tokens_witness_a, self.tokens_witness_b, 0, 0)
        return alignment
        

    def add_to_superbase(self, witness_a, witness_b, x, y):
#         print self.last_x - x - 1, self.last_y - y - 1
        if self.last_x - x - 1 > 0 or self.last_y - y - 1 > 0:
#             print x, self.last_x, y, self.last_y 
            # create new segment
            omitted_base = witness_a[x:self.last_x - 1]
#             print omitted_base
            added_witness = witness_b[y:self.last_y - 1]
#             print added_witness
            self.new_superbase = added_witness + self.new_superbase
            self.new_superbase = omitted_base + self.new_superbase

    def _process_cell(self, token_to_vertex, witness_a, witness_b, alignment, x, y):
        cell = self.table[y][x]
        # process segments
        if cell.match == True:
            self.add_to_superbase(witness_a, witness_b, x, y)
            self.last_x = x
            self.last_y = y
        # process alignment
        if cell.match == True:
            token = witness_a[x-1]
            token2 = witness_b[y-1]
            vertex = token_to_vertex[token]
            alignment[token2] = vertex
#             print("match")
#             print(token2)
            self.new_superbase.insert(0, token)
        return cell

    # This function traverses the table diagonally and scores each cell.
    # Original function from Mark Byers; translated from C into Python.
    def traverse_diagonally(self):
        m = self.length_witness_b+1
        n = self.length_witness_a+1
        for _slice in range(0, m + n - 1, 1):
            z1 = 0 if _slice < n else _slice - n + 1;
            z2 = 0 if _slice < m else _slice - m + 1;
            j = _slice - z2
            while j >= z1:
                x = _slice - j
                y = j
                self.score_cell(y, x)
                j -= 1

    def score_cell(self, y, x):
        # initialize root node score to zero (no edit operations have
        # been performed)
        if y == 0 and x == 0:
            self.table[y][x].g = 0
            return 
        # examine neighbor nodes
        nodes_to_examine = Set()
        # fetch existing score from the left node if possible
        if x > 0:
            nodes_to_examine.add(self.table[y][x-1])
        if y > 0:
            nodes_to_examine.add(self.table[y-1][x])
        if x > 0 and y > 0:
            nodes_to_examine.add(self.table[y-1][x-1])
        # calculate the maximum scoring parent node
        parent_node = max(nodes_to_examine, key=lambda x: x.g)
        if parent_node == self.table[y-1][x-1]:
            edit_operation = 0
        else:
            edit_operation = 1
        token_a = self.tokens_witness_a[x-1]
        token_b = self.tokens_witness_b[y-1]
        self.scorer.score_cell(self.table[y][x], parent_node, token_a, token_b, y, x, edit_operation)

    def _debug_edit_graph_table(self, table):
        # print the table horizontal
        x = PrettyTable()
        x.header=False
        for y in xrange(0, len(table)):
            cells = table[y]
            x.add_row(cells)
        # alignment can only be set after the field names are known.
        # since add_row sets the field names, it has to be set after x.add_row(cells)
        x.align="l"
        print(x)
        return x
    

