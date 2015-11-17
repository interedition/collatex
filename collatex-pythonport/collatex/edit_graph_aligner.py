'''
Created on Aug 5, 2014

@author: Ronald Haentjens Dekker
'''
from prettytable import PrettyTable

from collatex.core_classes import CollationAlgorithm
from collatex.suffix_based_scorer import Scorer
from collatex.tokenindex import TokenIndex
from collatex.transposition_handling import TranspositionDetection


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
    def __init__(self, collation, near_match=False, debug_scores=False, detect_transpositions=False, properties_filter=None):
        self.collation = collation
        self.debug_scores = debug_scores
        self.detect_transpositions = detect_transpositions
        self.token_index = TokenIndex(collation.witnesses)
        self.scorer = Scorer(self.token_index, near_match=near_match, properties_filter=properties_filter)
        self.align_function = self._align_table

    def collate(self, graph, collation):
        """
        :type graph: VariantGraph
        :type collation: Collation
        """
        # prepare the token index
        self.token_index.prepare()

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

            if self.detect_transpositions:
                detector = TranspositionDetection(self)
                detector.detect()

        if self.debug_scores:
            self._debug_edit_graph_table(self.table)
        

    def _align_table(self, superbase, witness, token_to_vertex):
        self.tokens_witness_a = superbase
        self.tokens_witness_b = witness.tokens()
        self.length_witness_a = len(self.tokens_witness_a)
        self.length_witness_b = len(self.tokens_witness_b)
        self.table = [[EditGraphNode() for _ in range(self.length_witness_a+1)] for _ in range(self.length_witness_b+1)]

        # per diagonal calculate the score (taking into account the three surrounding nodes)
        self.traverse_diagonally()

        alignment = {}
        self.additions = []
        self.omissions = []

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
            nodes_to_examine = set()
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
        # detect additions/omissions compared to the superbase
        # print self.last_x - x - 1, self.last_y - y - 1
        if self.last_x - x - 1 > 0 or self.last_y - y - 1 > 0:
            # print x, self.last_x, y, self.last_y
            # create new segment
            omitted_base = witness_a[x:self.last_x - 1]
            # print omitted_base
            # add segment to the omissions
            self.omissions.extend(omitted_base)
            added_witness = witness_b[y:self.last_y - 1]
            # print added_witness
            self.additions.extend(added_witness)
            # update superbase with additions, omissions
            self.new_superbase = added_witness + self.new_superbase
            self.new_superbase = omitted_base + self.new_superbase

    def _process_cell(self, token_to_vertex, witness_a, witness_b, alignment, x, y):
        cell = self.table[y][x]
        # process segments
        if cell.match:
            self.add_to_superbase(witness_a, witness_b, x, y)
            self.last_x = x
            self.last_y = y
        # process alignment
        if cell.match:
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
        nodes_to_examine = set()
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
        for y in range(0, len(table)):
            cells = table[y]
            x.add_row(cells)
        # alignment can only be set after the field names are known.
        # since add_row sets the field names, it has to be set after x.add_row(cells)
        x.align="l"
        print(x)
        return x
    

