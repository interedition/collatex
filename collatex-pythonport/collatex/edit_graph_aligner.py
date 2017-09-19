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
        return self.index == other.index & self.rank == other.rank

    def __hash__(self):
        return 10 * self.index + self.rank


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

    @staticmethod
    def has_tokens(vertex):
        return not vertex.tokens().isEmpty()

    def has_match(self, y, x):
        c = MatchCoordinate(y, x)
        return c in self.matches

    def match(self, y, x):
        c = MatchCoordinate(y, x)
        return self.matches[c]


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

    def __repr__(self):
        return str.format("({},{})", self.global_score, self.type.name)


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
            return Score(ScoreType.match, x, y, parent, parent.global_score + 1)
        return Score(ScoreType.mismatch, x, y, parent, parent.global_score - 1)

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
        self.x = len(score_matrix[0]) - 1
        self.y = len(score_matrix) - 1

    def __iter__(self):
        return self

    def _has_next(self):
        return not (self.x == 0 and self.y == 0)

    def __next__(self):
        if self._has_next():
            current_score = self.score_matrix[self.y][self.x]
            self.x = current_score.previous_x
            self.y = current_score.previous_y
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
        # self.align_function = self._align_table
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
        # self.update_token_to_vertex_array(tokens, first_witness)

        # align witness 2 - n
        for x in range(1, len(self.collation.witnesses)):
            next_witness = self.collation.witnesses[x]

            variant_graph_ranking = VariantGraphRanking.of(graph)
            variant_graph_ranks = list(set(map(lambda v: variant_graph_ranking.byVertex.get(v), graph.vertices())))
            # we leave in the rank of the start vertex, but remove the rank of the end vertex
            variant_graph_ranks.pop()

            # now the vertical stuff
            tokens_as_index_list = self.as_index_list(tokens)

            match_cube = MatchCube(self.token_index, next_witness, self.vertex_array, variant_graph_ranking)
            self.fill_needleman_wunsch_table(variant_graph_ranks, tokens, tokens_as_index_list, match_cube)

            aligned = self.align_matching_tokens(match_cube)
            self.merge(graph, tokens, aligned)

            # alignment = self.align_function(superbase, next_witness, token_to_vertex, match_cube)

            # merge
            token_to_vertex.update(self.merge(graph, next_witness.sigil, next_witness.tokens(), aligned))

            #             print("actual")
            #             self._debug_edit_graph_table(self.table)
            #             print("expected")
            #             self._debug_edit_graph_table(self.table2)

            # change superbase
            # superbase = self.new_superbase

            if self.detect_transpositions:
                detector = TranspositionDetection(self)
                detector.detect()

                # if self.debug_scores:
                #     self._debug_edit_graph_table(self.table)

    @staticmethod
    def as_index_list(tokens):
        tokens_as_index_list = [0]
        counter = 1
        for t in tokens:
            tokens_as_index_list.append(counter)
            counter += 1
        return tokens_as_index_list

    def fill_needleman_wunsch_table(self, variant_graph_ranks, next_witness, tokens_as_index_list, match_cube):
        self.cells = [[None for row in range(0, len(tokens_as_index_list))] for col in
                      range(0, len(variant_graph_ranks))]
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

        _debug_cells(self.cells)

        # fill the remaining cells
        # fill the rest of the cells in a y by x fashion
        for y in range(1, len(tokens_as_index_list)):
            for x in range(1, len(variant_graph_ranks)):
                previous_y = y - 1
                previous_x = x - 1
                from_upper_left = scorer.score(x, y, self.cells[previous_y][previous_x])
                from_left = scorer.gap(x, y, self.cells[y][previous_x])
                from_upper = self.calculate_from_upper(scorer, y, x, previous_y, match_cube)
                max_score = max(from_upper_left, from_left, from_upper, key=lambda s: s.global_score)
                self.cells[y][x] = max_score

    def calculate_from_upper(self, scorer, y, x, previous_y, match_cube):
        upper_is_match = match_cube.has_match(previous_y - 1, x - 1)
        if upper_is_match:
            return scorer.score(x, y, self.cells[previous_y][x])
        else:
            return scorer.gap(x, y, self.cells[previous_y][x])

    def align_matching_tokens(self, cube):
        #  using the score iterator..
        #  find all the matches
        #  later for the transposition detection, we also want to keep track of all the additions, omissions, and replacements
        aligned = {}
        scores = ScoreIterator(self.cells)
        matched_vertices = []
        for score in scores:
            if score.type == ScoreType.match:
                rank = score.x - 1
                match = cube.get_match(score.y - 1, rank)
                if match.vertex not in matched_vertices:
                    aligned[match.token] = match.vertex
                    matched_vertices.append(match.vertex)
        return aligned

        # def update_token_to_vertex_array(self, tokens, witness):
        #     # we need to update the token -> vertex map
        #     # that information is stored in protected map
        #     token_position = self.token_index.start_token_position_for_witness(witness)
        #     for token in tokens:
        #         vertex = super.witness_token_vertices[token]
        #         self.vertex_array[token_position] = vertex
        #         token_position += 1


def _debug_cells(cells):
    y = 0
    for row in cells:
        x = 0
        print()
        for cell in row:
            if cell is not None:
                print(str.format("[{},{}]:{}", x, y, cell))
            x += 1
        y += 1
