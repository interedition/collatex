"""Functions and objects for near matching
    Called by: collate() (in core_functions.py) with near_match=True, segmentation=False
"""
from collatex.core_classes import VariantGraphRanking
import Levenshtein
from networkx.algorithms.dag import topological_sort


# Flatten a list of lists (goes only one level down)
def flatten(in_list):
    return [item for sublist in in_list for item in sublist]


def perform_near_match(graph, ranking):
    # Walk ranking table in reverse order and add near-match edges to graph
    reverse_topological_sorted_vertices = topological_sort(graph.graph, reverse=True)
    for v in reverse_topological_sorted_vertices:
        target_rank = ranking.byVertex[v]
        in_edges = graph.in_edges(v)
        if len(in_edges) > 1:
            move_candidates = [in_edge[0] for in_edge in in_edges \
                               if target_rank > ranking.byVertex[in_edge[0]] + 1]
            for move_candidate in move_candidates:
                move_candidate_witnesses = set(move_candidate.tokens)
                min_rank = ranking.byVertex[move_candidate]
                max_rank = target_rank - 1
                vertices_to_compare = flatten([ranking.byRank[r] for r in range(min_rank, max_rank + 1)])
                vertices_to_compare.remove(move_candidate)
                # TODO: don't draw a near-match edge if both comparands include any of the same witnesses
                for vertex_to_compare in vertices_to_compare:
                    if not move_candidate_witnesses.intersection(vertex_to_compare.tokens):
                        print('now comparing move candidate ', move_candidate, ' (witnesses ', move_candidate_witnesses,\
                              ') with ', vertex_to_compare, ' (witnesses ', vertex_to_compare.tokens, ')')
                        ratio = Levenshtein.ratio(str(move_candidate), str(vertex_to_compare))
                        graph.connect_near(vertex_to_compare, move_candidate, ratio)
    # Create new ranking table (passed along to creation of alignment table)
    return VariantGraphRanking.of(graph)
