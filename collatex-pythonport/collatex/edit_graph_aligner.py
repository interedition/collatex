'''
Created on Aug 5, 2014

@author: Ronald Haentjens Dekker
'''

from collatex.collatex_core import CollationAlgorithm
from sets import Set

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
    THIS CLASS IS ONLY HERE TO SHOW HOW THE SCORING WORKS AND THAT EVERY POSSIBLE COMBINATION IS CONSIDERED!
    THE A* BASED ALIGNER WILL BE MUCH FASTER!
    '''
class EditGraphAligner(CollationAlgorithm):
    def __init__(self, collation):
        self.collation = collation

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

        # construct superbase
        superbase = tokens
        
        # second witness
        next_witness = collation.witnesses[1]
        
        # alignment = token -> vertex
        alignment = self._align(superbase, next_witness, token_to_vertex)
        
        # merge
        token_to_vertex = self.merge(graph, next_witness.sigil, next_witness.tokens(), alignment)

    def _align(self, superbase, witness, token_to_vertex):
        self.tokens_witness_a = superbase
        self.tokens_witness_b = witness.tokens()
        self.length_witness_a = len(self.tokens_witness_a)
        self.length_witness_b = len(self.tokens_witness_b)
        self.table = [[EditGraphNode() for _ in range(self.length_witness_a+1)] for _ in range(self.length_witness_b+1)]

        # per diagonal calculate the score (taking into account the three surrounding nodes)
        self.traverse_diagonally()

        alignment = {}

        # start lower right cell
        x = self.length_witness_a
        y = self.length_witness_b
        while x > 0 and y > 0:
            cell = self._process_cell(token_to_vertex, self.tokens_witness_a, self.tokens_witness_b, alignment, x, y)
            
            # work our way to the upper left
            if self.table[y-1][x-1].g <= cell.g:
                # another match
                y = y -1
                x = x -1
            if self.table[y-1][x].g <= cell.g:
                #omisison?
                y = y -1
            if self.table[y][x-1].g <= cell.g:
                #addition?
                x = x -1
                
        return alignment
        
    def _process_cell(self, token_to_vertex, witness_a, witness_b, alignment, x, y):
        cell = self.table[y][x]
        if cell.match == True:
            token = witness_a[x-1]
            token2 = witness_b[y-1]
            vertex = token_to_vertex[token]
            alignment[token2] = vertex
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
                self.table[j][_slice-j].g=self.score(j, _slice - j)
                j -= 1


    # TODO: Count and score segments
    def score(self, y, x):
        # initialize root node score to zero (no edit operations have
        # been performed)
        if y == 0 and x == 0:
            return 0 
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
        # no matching possible in this case (always treated as a gap)
        # it is either an add or a delete
        if x == 0 or y == 0:
            return parent_node.g - 1
         
        # it is either an add/delete or replacement (so an add and a delete)
        # it is a replacement
        if parent_node == self.table[y-1][x-1]:
            # now we need to determine whether this node represents a match
            token_a = self.tokens_witness_a[x-1]
            token_b = self.tokens_witness_b[y-1]
            match = token_a.token_string == token_b.token_string
            # based on match or not and parent_node calculate new score
            if match:
                # mark the fact that this node is match
                self.table[y][x].match = True
                # do not change score for now 
                score = parent_node.g
                # count segments
                if parent_node.match == False:
                    self.table[y][x].segments = parent_node.segments + 1
             
            else:
                score = parent_node.g - 2
        # it is an add/delete
        else:
            score = parent_node.g - 1
        return score

