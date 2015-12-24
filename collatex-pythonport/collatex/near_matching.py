"""Functions and objects for near matching
    Called by: collate_pretokenized_json_nearMatch() and collate_nearMatch() (in core_functions.py)
"""
from Levenshtein import distance


def process_rank(rank, collation, ranking, witness_count):
    nodes_at_rank = ranking.byRank[rank]
    witnesses_at_rank = []
    for this_node in nodes_at_rank:
        for key in this_node.tokens:
            witnesses_at_rank.append(str(key))
    witnesses_at_rank_count = sum([len(thisNode.tokens) for thisNode in nodes_at_rank])
    if witnesses_at_rank_count == witness_count:
        pass
    else:
        missing_witnesses = set([witness.sigil for witness in collation.witnesses]) - set(witnesses_at_rank)
        witnesses_weve_seen = set()
        for missingWitness in missing_witnesses:
            if missingWitness not in witnesses_weve_seen:
                (prior_rank, prior_node) = find_prior_node(missingWitness, rank, ranking)
                if prior_rank:
                    prior_node_witnesses = prior_node.tokens.keys()
                    witnesses_weve_seen = witnesses_weve_seen.union(prior_node_witnesses)

                    left = NearMatchTable(ranking, prior_rank, prior_node)
                    right = NearMatchTable(ranking, rank, prior_node)
                    if right.return_values < left.return_values:
                        # if (right_min, -right_max_count) < (left_min, -left_max_count):
                        # move the entire node from prior_rank to (current) rank
                        ranking.byRank[prior_rank].remove(prior_node)
                        ranking.byRank[rank].append(prior_node)
                        ranking.byVertex[prior_node] = rank
    return rank


def find_prior_node(witness, current_rank, ranking):
    for rank in range(current_rank - 1, 1, -1):
        nodes_at_rank = ranking.byRank[rank]
        for this_node in nodes_at_rank:
            for key in this_node.tokens:
                if witness == key:  # Worst case: will be found at start if not on a real node
                    return rank, this_node
    # The start node has no witnesses, so return a special value to indicate nothing found
    return None, None


class NearMatchTable(object):
    def __init__(self, ranking, rank, prior_node):
        self.table = {}
        self._construct_table(ranking, rank, prior_node)

    def _construct_table(self, ranking, rank, prior_node):
        for current_node in ranking.byRank[rank]:
            if current_node != prior_node:
                self.table[current_node.label] = distance(current_node.label, prior_node.label), len(
                    current_node.tokens)

    @property
    def return_values(self):
        min_distance = min((value[0] for value in self.table.values()))
        max_witness_count = max((value[1] for value in self.table.values()))
        return min_distance, -max_witness_count
