'''
Created on Aug 5, 2014

@author: Ronald Haentjens Dekker
'''
from enum import Enum

from prettytable import PrettyTable

from collatex.core_classes import CollationAlgorithm, VariantGraphRanking, VariantGraph
from collatex.suffix_based_scorer import Scorer
from collatex.tokenindex import TokenIndex
from collatex.transposition_handling import TranspositionDetection


class EditGraphNode(object):
    def __init__(self):
        self.g = 0  # global score
        self.segments = 0  # number of segments
        self.match = False  # this node represents a match or not

    def __repr__(self):
        return repr(self.g)

    '''
    Aligner based on an edit graph.
    It needs a g function and a definition of a match.
    Since every node of the graph has three children the graph is represented as a table internally.
    Default implementation is a* based.
    '''


class Match(object):
    def __init__(self, vertex, token):
        self.vertex = vertex
        self.token = token


class MatchCoordinate():
    def __init__(self, row, rank):
        self.index = row  # position in witness, starting from zero
        self.rank = rank  # rank in the variant graph

    def __eq__(self, other):
        self.index == other.index & self.rank == other.rank


class MatchCube():
    def __init__(self, token_index, witness, vertex_array, variant_graph_ranking):
        self.matches = {}
        start_token_position_for_witness = token_index.start_token_position_for_witness(witness)
        instances = token_index.block_instances_for_witness(witness)
        for witness_instance in instances:
            block = witness_instance.block
            all_instances = block.all_instances
            graph_instances = [i for i in all_instances if i.start_token < start_token_position_for_witness]
            for graph_instance in graph_instances:
                graph_start_token = graph_instance.start_token
                for i in range(0, block.length):
                    # for (int i = 0; i < block.length; i++) {
                    v = vertex_array[graph_start_token + i]
                    if v is None:
                        raise "Vertex is null for token \"" + graph_start_token + i + "\" that is supposed to be mapped to a vertex in the graph!"

                    rank = variant_graph_ranking.apply(v) - 1
                    witness_start_token = witness_instance.start_token + i
                    row = witness_start_token - start_token_position_for_witness
                    token = token_index.token_array[witness_start_token]
                    match = Match(v, token)
                    coordinate = MatchCoordinate(row, rank)
                    self.matches[coordinate] = match

    def has_tokens(vertex):
        not vertex.tokens().isEmpty()

    def has_match(self, y, x):
        c = MatchCoordinate(y, x)
        self.matches.containsKey(c)

    def match(self, y, x):
        c = MatchCoordinate(y, x)
        self.matches.get(c)


class ScoreType(Enum):
    match = 1
    mismatch = 2
    addition = 3
    deletion = 4
    empty = 5


class Score():
    def __init__(self, score_type, x, y, parent, global_score=None):
        self.type = score_type
        self.x = x
        self.y = y
        self.parent = parent
        self.previous_x = 0 if (parent is None) else parent.x
        self.previous_y = 0 if (parent is None) else parent.y
        self.global_score = parent.global_score if global_score is None else global_score


class Scorer:
    def __init__(self, match_cube=None):
        self.match_cube = match_cube

    def gap(self, x, y, parent):
        score_type = self.determine_type(x, y, parent)
        return Score(score_type, x, y, parent, parent.global_score - 1)

    def score(self, x, y, parent):
        rank = x - 1
        if self.match_cube.has_match(y - 1, rank):
            match = self.matchCube.get_match(y - 1, rank)
            return Score(ScoreType.match, x, y, parent, parent.globalScore + 1)
        return Score(ScoreType.mismatch, x, y, parent, parent.globalScore - 1)

    @staticmethod
    def determine_type(x, y, parent):
        if x == parent.x:
            return ScoreType.addition
        if y == parent.y:
            return ScoreType.deletion
        return ScoreType.empty

    class ScoreIterator:
        def __init__(self, score_matrix):
            self.score_matrix = score_matrix
            self.x = score_matrix[0].length - 1
            self.y = score_matrix.length - 1

        def __iter__(self):
            return self

        def _has_next(self):
            return not (self.x == 0 and self.y == 0)

        def next(self):
            if self._has_next():
                current_score = self.matrix[self.y][self.x]
                self.x = current_score.previousX
                self.y = current_score.previousY
                return current_score
            else:
                raise StopIteration()


class EditGraphAligner(CollationAlgorithm):
    def __init__(self, collation, near_match=False, debug_scores=False, detect_transpositions=False,
                 properties_filter=None):
        self.scorer = Scorer()
        self.collation = collation
        self.debug_scores = debug_scores
        self.detect_transpositions = detect_transpositions
        self.token_index = TokenIndex(collation.witnesses)
        # self.scorer = Scorer(self.token_index, near_match=near_match, properties_filter=properties_filter)
        self.align_function = self._align_table
        self.added_witness = []
        self.omitted_base = []
        self.vertex_array = []
        self.cells = [[]]

    def collate(self, graph):
        """
        :type graph: VariantGraph
        """
        # prepare the token index
        self.token_index.prepare()

        # Build the variant graph for the first witness
        # this is easy: generate a vertex for every token
        first_witness = self.collation.witnesses[0]
        tokens = first_witness.tokens()
        token_to_vertex = self.merge(graph, first_witness.sigil, tokens)

        # construct superbase
        superbase = tokens

        # align witness 2 - n
        for x in range(1, len(self.collation.witnesses)):
            next_witness = self.collation.witnesses[x]

            variant_graph_ranking = VariantGraphRanking.of(graph)
            variant_graph_ranks = list(set(map(lambda v: variant_graph_ranking.byVertex.get(v), graph.vertices())))
            # we leave in the rank of the start vertex, but remove the rank of the end vertex
            variant_graph_ranks.pop()

            # now the vertical stuff
            tokens_as_index_list = self.as_index_list(tokens)

            match_cube = MatchCube(self.token_index, next_witness, self.vertex_array, variant_graph_ranking);
            self.fill_needleman_wunsch_table(variant_graph_ranks, next_witness, tokens_as_index_list, match_cube);
            self.scorer.match_cube = match_cube

            alignment = self.align_function(superbase, next_witness, token_to_vertex, match_cube)

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

    def _align_table(self, superbase, witness, token_to_vertex, match_cube):
        if not superbase:
            raise Exception("Superbase is empty!")

        # print(""+str(superbase)+":"+str(witness.tokens()))
        self.tokens_witness_a = superbase
        self.tokens_witness_b = witness.tokens()
        self.length_witness_a = len(self.tokens_witness_a)
        self.length_witness_b = len(self.tokens_witness_b)
        self.table = [[EditGraphNode() for _ in range(self.length_witness_a + 1)] for _ in
                      range(self.length_witness_b + 1)]

        # per diagonal calculate the score (taking into account the three surrounding nodes)
        self.traverse_diagonally()

        alignment = {}
        self.additions = []
        self.omissions = []
        self.new_superbase = []

        # start lower right cell
        x = self.length_witness_a
        y = self.length_witness_b
        # work our way to the upper left
        while x > 0 and y > 0:
            cell = self.table[y][x]
            self._process_cell(token_to_vertex, self.tokens_witness_a, self.tokens_witness_b, alignment, x, y)
            # examine neighbor nodes
            nodes_to_examine = set()
            nodes_to_examine.add(self.table[y][x - 1])
            nodes_to_examine.add(self.table[y - 1][x])
            nodes_to_examine.add(self.table[y - 1][x - 1])
            # calculate the maximum scoring parent node
            parent_node = max(nodes_to_examine, key=lambda x: x.g)
            # move position
            if self.table[y - 1][x - 1] == parent_node:
                # another match or replacement
                if not cell.match:
                    self.omitted_base.insert(0, self.tokens_witness_a[x - 1])
                    self.added_witness.insert(0, self.tokens_witness_b[y - 1])
                    # print("replacement:"+str(self.tokens_witness_a[x-1])+":"+str(self.tokens_witness_b[y-1]))
                    # else:
                    # print("match:"+str(self.tokens_witness_a[x-1]))
                y -= 1
                x -= 1
            else:
                if self.table[y - 1][x] == parent_node:
                    # addition?
                    self.added_witness.insert(0, self.tokens_witness_b[y - 1])
                    # print("added:" + str(self.tokens_witness_b[y - 1]))
                    y -= 1
                else:
                    if self.table[y][x - 1] == parent_node:
                        # omission?
                        self.omitted_base.insert(0, self.tokens_witness_a[x - 1])
                        # print("omitted:" + str(self.tokens_witness_a[x - 1]))
                        x -= 1

        # process additions/omissions in the begin of the superbase/witness
        if x > 0:
            self.omitted_base = self.tokens_witness_a[0:x] + self.omitted_base
        if y > 0:
            self.added_witness = self.tokens_witness_b[0:y] + self.added_witness
        self.add_to_superbase()
        return alignment

    def add_to_superbase(self):
        if self.omitted_base or self.added_witness:
            # print("update superbase:" + str(self.omitted_base) + ":" + str(self.added_witness))
            # update superbase with additions, omissions
            self.new_superbase = self.added_witness + self.new_superbase
            self.new_superbase = self.omitted_base + self.new_superbase
            self.added_witness = []
            self.omitted_base = []

    def _process_cell(self, token_to_vertex, witness_a, witness_b, alignment, x, y):
        cell = self.table[y][x]
        if cell.match:
            # process segments
            self.add_to_superbase()
            # process alignment
            token = witness_a[x - 1]
            token2 = witness_b[y - 1]
            vertex = token_to_vertex[token]
            alignment[token2] = vertex
            #             print("match")
            #             print(token2)
            self.new_superbase.insert(0, token)
        return cell

    # This function traverses the table diagonally and scores each cell.
    # Original function from Mark Byers; translated from C into Python.
    def traverse_diagonally(self):
        m = self.length_witness_b + 1
        n = self.length_witness_a + 1
        for _slice in range(0, m + n - 1, 1):
            z1 = 0 if _slice < n else _slice - n + 1
            z2 = 0 if _slice < m else _slice - m + 1
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
            nodes_to_examine.add(self.table[y][x - 1])
        if y > 0:
            nodes_to_examine.add(self.table[y - 1][x])
        if x > 0 and y > 0:
            nodes_to_examine.add(self.table[y - 1][x - 1])
        # calculate the maximum scoring parent node
        parent_node = max(nodes_to_examine, key=lambda x: x.g)
        if parent_node == self.table[y - 1][x - 1]:
            edit_operation = 0
        else:
            edit_operation = 1
        token_a = self.tokens_witness_a[x - 1]
        token_b = self.tokens_witness_b[y - 1]
        self.scorer.score_cell(self.table[y][x], parent_node, token_a, token_b, y, x, edit_operation)

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

    @staticmethod
    def as_index_list(tokens):
        tokens_as_index_list = [0]
        counter = 1
        for t in tokens:
            tokens_as_index_list.append(counter)
            counter += 1
        return tokens_as_index_list

    def fill_needleman_wunsch_table(self, variant_graph_ranks, next_witness, tokens_as_index_list, match_cube):
        # self.cells = [][]
        scorer = Scorer(match_cube)

        # init 0,0
        self.cells[0][0] = Score(ScoreType.empty, 0, 0, None, 0)

        # fill the first row with gaps
        for x in range(1, len(variant_graph_ranks)):
            previous_x = x - 1
            self.cells[0][x] = scorer.gap(x, 0, self.cells[0][previous_x])

        # fill the first column with gaps
        for y in range(1, len(tokens_as_index_list)):
            previous_y = y - 1
            self.cells[y][0] = scorer.gap(0, y, self.cells[previous_y][0])

        # fill the remaining cells
        # fill the rest of the cells in a y by x fashion
        for y in range(1, len(tokens_as_index_list)):
            for x in range(1, len(variant_graph_ranks)):
                witness_token = next_witness.get(y - 1)
                previous_y = y - 1
                previous_x = x - 1
                from_upper_left = scorer.score(x, y, self.cells[previous_y][previous_x])
                from_left = scorer.gap(x, y, self.cells[y][previous_x])
                from_upper = self.calculate_from_upper(scorer, y, x, previous_y, witness_token, match_cube)
                max_score = max(from_upper_left, from_left, from_upper, key=lambda s: s.global_score)
                self.cells[y][x] = max_score

    def calculate_from_upper(self, scorer, y, x, previous_y, witness_token, match_cube):
        upper_is_match = match_cube.has_match(previous_y - 1, x - 1);
        if upper_is_match:
            return scorer.score(x, y, self.cells[previous_y][x])
        else:
            return scorer.gap(x, y, self.cells[previous_y][x]);
