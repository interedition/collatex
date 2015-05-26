'''
Created on Aug 4, 2014

@author: Ronald Haentjens Dekker
'''
from prettytable import PrettyTable

from collatex.astar import AStarNode, AStar
from collatex.core_classes import CollationAlgorithm
from collatex.suffix_based_scorer import Scorer


class ExperimentalAstarAligner(CollationAlgorithm):
    def __init__(self, collation, near_match=False, astar=False, debug_scores=False):
        self.collation = collation
        self.debug_scores = debug_scores
        self.scorer = Scorer(collation, near_match)
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

    def _debug_edit_graph_table(self, table):
        # print the table horizontal
        x = PrettyTable()
        x.header = False
        for y in range(0, len(table)):
            cells = table[y]
            x.add_row(cells)
        # alignment can only be set after the field names are known.
        # since add_row sets the field names, it has to be set after x.add_row(cells)
        x.align = "l"
        print(x)
        return x

    # method is here for debug purposes
    # at no time in real life a complete table is needed
    def _create_heuristic_table(self, superbase, witness):
        self.tokens_witness_a = superbase
        self.tokens_witness_b = witness.tokens()
        self.length_witness_a = len(self.tokens_witness_a)
        self.length_witness_b = len(self.tokens_witness_b)
        aligner = AstarEditGraphAligner(self.tokens_witness_a, self.tokens_witness_b, self.scorer)
        self.table = [[AstarEditGraphNode(aligner, y, x) for x in range(self.length_witness_a + 1)] for y in
                      range(self.length_witness_b + 1)]
        self.heuristic_table = [[0 for x in range(self.length_witness_a + 1)] for y in range(self.length_witness_b + 1)]
        for y in range(self.length_witness_b + 1):
            for x in range(self.length_witness_a + 1):
                # TODO: I could create node dynamically
                # TODO: creating empty integer is also not really needed
                self.heuristic_table[y][x] = aligner.heuristic(self.table[y][x])

    def _align_astar(self, superbase, witness, token_to_vertex, control_table=None):
        self.tokens_witness_a = superbase
        self.tokens_witness_b = witness.tokens()
        self.length_witness_a = len(self.tokens_witness_a)
        self.length_witness_b = len(self.tokens_witness_b)
        self.control_table = control_table
        aligner = AstarEditGraphAligner(self.tokens_witness_a, self.tokens_witness_b, self.scorer)
        self.table = [[AstarEditGraphNode(aligner, y, x) for x in range(self.length_witness_a + 1)] for y in
                      range(self.length_witness_b + 1)]
        aligner.table = self.table
        start = self.table[0][0]
        path = aligner.search(start, self.control_table)
        self._debug_path = path

        # transform path into an alignment
        alignment = {}

        # segment stuff
        # note we traverse from left to right!
        self.last_x = 0
        self.last_y = 0
        self.new_superbase = []

        for element in path:
#             print(element.y, element.x)

            if element.match == True:
                # process segments
                self.newer_add_to_superbase(self.tokens_witness_a, self.tokens_witness_b, element.x, element.y)
                self.last_x = element.x
                self.last_y = element.y
                # process alignment
                token = self.tokens_witness_a[element.x - 1]
                token2 = self.tokens_witness_b[element.y - 1]
                vertex = token_to_vertex[token]
                alignment[token2] = vertex
                # add match to superbase
                self.new_superbase.append(token)

        # process additions/omissions in the begin of the superbase/witness
        self.newer_add_to_superbase(self.tokens_witness_a, self.tokens_witness_b, self.length_witness_a,
                                    self.length_witness_b)
        return alignment

    def newer_add_to_superbase(self, witness_a, witness_b, x, y):
        if x - self.last_x - 1 > 0 or y - self.last_y - 1 > 0:
            # create new segment
            omitted_base = witness_a[self.last_x:x - 1]
            added_witness = witness_b[self.last_y:y - 1]
            self.new_superbase += omitted_base
            self.new_superbase += added_witness



class AstarEditGraphNode(AStarNode):
    def __init__(self, aligner, y, x):
        self.aligner = aligner
        self.y = y
        self.x = x
        self.match = False
        self.segments = 0  # TODO: remove
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
        # NOTE: possible optimization: you don't always need to fetch the tokens!
        token_a = self.aligner.tokens_witness_a[other.x - 1]
        token_b = self.aligner.tokens_witness_b[other.y - 1]
        edit_operation = 1
        if other.x - 1 == self.x and other.y - 1 == self.y:
            edit_operation = 0
        # def score_cell(self, table_node, parent_node, token_a, token_b, y, x, edit_operation):
        self.aligner.scorer.score_cell(other, self, token_a, token_b, other.y, other.x, edit_operation)
        cost = other.g - self.g
        #         print("From Node: "+str(self.y)+" "+str(self.x)+" "+str(self.edit_operation)+" to other: "+str(other.y)+" "+str(other.x)+" "+str(other.edit_operation)+" cost: "+str(cost))
        return -cost


class AstarEditGraphAligner(AStar):
    def __init__(self, tokens_witness_a, tokens_witness_b, scorer):
        self.tokens_witness_a = tokens_witness_a
        self.tokens_witness_b = tokens_witness_b
        self.scorer = scorer
        self.table = None  # TODO: not nice!
        self.length_witness_a = len(self.tokens_witness_a)
        self.length_witness_b = len(self.tokens_witness_b)

    def create_childnodes(self, node):
        # we create 3 child nodes
        # NOTE: In some cases it is possible to get away with only 2 nodes
        # NOTE: possible performance enhancement
        child_nodes = []
        if node.y < self.length_witness_b:
            child_node = self.table[node.y + 1][node.x]
            child_nodes.append(child_node)
        if node.x < self.length_witness_a:
            child_node = self.table[node.y][node.x + 1]
            child_nodes.append(child_node)
        if node.y < self.length_witness_b and node.x < self.length_witness_a:
            child_node = self.table[node.y + 1][node.x + 1]
            child_nodes.append(child_node)
        return child_nodes

    # We delegate the heuristic scoring to the Scorer (who has knowledge of the blocks)
    def heuristic(self, node):
        return self.scorer.heuristic_score(node, self.length_witness_b, self.length_witness_b)


# The second parameter could also be the tree
class DecisionTreeNode(AStarNode):
    def __init__(self, aligner):
        self.pointer_a = 0
        self.pointer_b = 0
        self.aligner = aligner
        # call super
        super(DecisionTreeNode, self).__init__()

    def is_end_node(self):
        # TODO: tokens recalculated
        # better to do this with iteration
        # This can then be written without if statements
        len_wit_a = len(self.aligner.witness_a.tokens())
        if self.pointer_a == len_wit_a:
            return True
        len_wit_b = len(self.aligner.witness_b.tokens())
        if self.pointer_b == len_wit_b:
            return True
        return False


class DecisionTree(AStar):
    def __init__(self, aligner):
        self.aligner = aligner

    def create_childnodes(self):
        #         # check whether a token is a match
        #         token_a = tokens_a[self.state.pointer_a]
        #         token_b = tokens_b[self.state.pointer_b]
        #         match = token_a.eql(token_b)
        #
        #         #move this to above!
        #         if not match:
        #             # we try to handle mismatch as an omission
        #             self.states
        #
        pass


class Aligner(object):
    '''
    Decision Tree based aligner
    This is a prototype: 
    There are limitations
    At first it only works with just two witnesses
    It works with two pointers.
    The pointers start at the beginning (first token) of each of the witnesses.
    If this prototype works, one of the witnesses has to be replaced by the graph
    Then if that works, the enhanced suffix array based optimalization has to be integrated.
    Also we start by tracking one decision state.. we should keep track of more state
    to reach the right conclusion. The A* algorithm works very well for that.
    '''

    def __init__(self, witness_a, witness_b):
        '''
        Constructor
        '''
        self.witness_a = witness_a
        self.witness_b = witness_b
        self.tree = DecisionTree(self)

    def align(self):
        '''
        Every step we have 3 choices:
        1) Move pointer witness a --> omission
        2) Move pointer witness b --> addition
        3) Move pointer of both witness a/b  --> match
        Note: a replacement is omission followed by an addition or the other way around
        
        Choice 1 and 2 are only possible if token a and b are not a match OR when tokens are repeated.
        For now I ignore token repetition..
        '''
        # extract tokens from witness (note that this can be done in a streaming manner if desired)
        tokens_a = self.witness_a.tokens()
        tokens_b = self.witness_b.tokens()
        # create virtual decision tree (nodes are created on demand)
        # see above
        # create start node
        start = DecisionTreeNode(self)

        # search the decision tree
        result = self.tree.search(start)
        print(result)

        pass
