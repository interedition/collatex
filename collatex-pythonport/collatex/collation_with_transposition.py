"""
Created on: 3 October 2020
@author: Ronald Haentjens Dekker

"""
from collatex.tokenindex import TokenIndex


def collate_with_transposition(collation):
    token_index = TokenIndex(collation.witnesses)
    token_index.prepare()
    # print(token_index)
    return token_index
    pass


# returns matches as (index -> index in the token array mapping)
def potential_token_to_token_matches(token_index, witness):
    instances = token_index.block_instances_for_witness(witness)
    # print("> token_index.witness_to_block_instances", token_index.witness_to_block_instances)
    # print("> instances", instances)
    start_token_position_for_witness = token_index.start_token_position_for_witness(witness)
    # print("> start_token_position_for_witness=", start_token_position_for_witness)
    return potential_token_to_token_matches_for_specific_instances(instances, start_token_position_for_witness)
    pass


def potential_token_to_token_matches_for_specific_instances(instances, start_token_position_for_witness):
    # array of token to token matches. Ints map to tokens in token array.
    matches = []
    for witness_instance in instances:
        # print("> witness_instance=", witness_instance)
        block = witness_instance.block
        all_instances = block.get_all_instances()
        instances_in_graph = [i for i in all_instances if i.start_token < start_token_position_for_witness]
        for instance_in_graph in instances_in_graph:
            graph_start_token = instance_in_graph.start_token
            for i in range(0, block.length):
                # print("> graph_start_token + i =", (graph_start_token + i))
                witness_start_token = witness_instance.start_token + i
                match = TokenToTokenMatch(graph_start_token + i, witness_start_token)
                matches.append(match)
    return matches


# Note: uses the positions in the token array to address the tokens
class TokenToTokenMatch(object):
    def __init__(self, token_position1, token_position2):
        self.token_position1 = token_position1
        self.token_position2 = token_position2

    def __repr__(self):
        return str.format("Match(token1={},token2={})", self.token_position1, self.token_position2)





