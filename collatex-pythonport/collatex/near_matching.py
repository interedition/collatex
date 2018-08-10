"""Functions and objects for near matching
    Called by: collate() (in core_functions.py) with near_match=True, segmentation=False
"""
from collatex.core_classes import VariantGraphRanking
import Levenshtein
from networkx.algorithms.dag import topological_sort
from collections import defaultdict


# Flatten a list of lists (goes only one level down)
def flatten(in_list):
    return [item for sublist in in_list for item in sublist]


# Returns set of witnesses on verticies at range of ranks
# ranking: VariantGraphRanking()
# min_rank: current rank of vertex being considered for movement (compare to other verticies at that rank)
# max_rank: ceiling _above_ maximum possible new rank
def witnesses_on_path(ranking, min_rank, max_rank):
    path_witnesses = set([])
    for rank in range(min_rank + 1, max_rank):
        # print(rank)
        for item in ranking.byRank[rank]:
            keys = (item.tokens.keys())
            for key in keys:
                path_witnesses.add(key)
    return path_witnesses

def perform_near_match(graph, ranking):
    # Walk ranking table in reverse order and add near-match edges to graph
    reverse_topological_sorted_vertices = reversed(list(topological_sort(graph.graph)))
    for v in reverse_topological_sorted_vertices:
        ##### Doesn't work:
        #         target_rank = ranking.byVertex[v] # get the rank of a vertex
        #
        # in_edges = graph.in_edges(v) # if it has more than one in_edge, perhaps something before it can be moved
        # if len(in_edges) > 1:
        #     # candidates for movement are the sources of in edges more than one rank earlir
        #     move_candidates = [in_edge[0] for in_edge in in_edges \
        #                        if target_rank > ranking.byVertex[in_edge[0]] + 1]
        #     for move_candidate in move_candidates:
        #         move_candidate_witnesses = set(move_candidate.tokens) # prepare to get intersection later
        #         min_rank = ranking.byVertex[move_candidate] # lowest possible rank is current position
        #         max_rank = target_rank - 1 # highest possible rank is one more before the target
        #         vertices_to_compare = flatten([ranking.byRank[r] for r in range(min_rank, max_rank + 1)])
        #         vertices_to_compare.remove(move_candidate) # don't compare it to itself
        #         print('comparing ', move_candidate, ' to ', vertices_to_compare)
        #         ratio_dict = {} # ratio:vertex_to_compare
        #         for vertex_to_compare in vertices_to_compare:
        #             # don't move if there's already a vertex there with any of the same witnesses
        #             if not move_candidate_witnesses.intersection(vertex_to_compare.tokens):
        #                 print('now comparing move candidate ', move_candidate, \
        #                       ' (witnesses ', move_candidate_witnesses,\
        #                       ') with ', vertex_to_compare, ' (witnesses ', vertex_to_compare.tokens, ')')
        #                 ratio = Levenshtein.ratio(str(move_candidate), str(vertex_to_compare))
        #                 ratio_dict[ratio] = vertex_to_compare
        #         # Create only winning edge; losing edges can create later cycles
        #         graph.connect_near(ratio_dict[max(ratio_dict)], move_candidate, ratio)
        #         print('connected ', move_candidate, ' to ', ratio_dict[max(ratio_dict)], \
        #               ' with ratio ', max(ratio_dict))
        ######
        in_edges = graph.in_edges(v, data=True)
        for source, target, edgedata in in_edges:
            # can only move if two conditions are both true:
            # 1) rank of source differs from v by more than 1; max target rank will be rank of v - 1
            # 2) out_edges from source must have no target at exactly one rank higher than source
            if ranking.byVertex[v] - ranking.byVertex[source] > 1 and \
                    1 not in [ranking.byVertex[v] - ranking.byVertex[u] for (u,v) in graph.out_edges(source)]:
                min_rank = ranking.byVertex[source]
                max_rank = ranking.byVertex[v]
                match_candidates = [item for item in flatten([ranking.byRank[rank] \
                                            for rank in range(min_rank, max_rank)]) if item is not source]
                # print(match_candidates)
                levenshtein_dict = defaultdict(list)
                for match_candidate in match_candidates:
                    ratio = Levenshtein.ratio(str(source), str(match_candidate))
                    # print(source, match_candidate, ratio)
                    levenshtein_dict[ratio].append(match_candidate)
                weight = max(levenshtein_dict)
                winner = levenshtein_dict[max(levenshtein_dict)][0]
                # print('weight:',weight,'source:',winner)
                graph.connect_near(winner,source,weight)
                # print('before: byRank',str(ranking.byRank))
                # print('before: byVertex',str(ranking.byVertex))
                # update ranking table for next pass through loop and verify
                ranking = VariantGraphRanking.of(graph)
                # print('after: byRank',str(ranking.byRank))
                # print('after: byVertex',str(ranking.byVertex))
    # Create new ranking table (passed along to creation of alignment table)
    return VariantGraphRanking.of(graph)
