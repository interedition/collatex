"""
Created on: 3 October 2020
@author: Ronald Haentjens Dekker

"""
from typing import List
import pandas as pd
from collatex.tokenindex import TokenIndex


def collate_with_transposition(collation):
    token_index = TokenIndex(collation.witnesses)
    token_index.prepare()
    # print(token_index)
    return token_index


# a token index has blocks.
# blocks have frequency etc.
# a block has instances.
# instances have tokens.
def find_most_unique_blocks_(token_index):
    blocks_as_list: List[List] = []
    for idx, block in enumerate(token_index.blocks):
        blocks_as_list.append([idx, repr(block.get_all_instances()[0]), block.get_frequency(), block.length,
                               block.get_depth(), block.get_depth()/block.get_frequency()*block.length])
        pass

    # print(blocks_as_list)

    df = pd.DataFrame(blocks_as_list,
                      index=[1, 2, 3, 4, 5, 6, 7], columns=['block_id', 'tokens', 'frequency', 'length',
                                                            'nr. of witnesses',
                                                            'uniqueness'])

    print(df)

    # We need to sort based on rarity. So lowest frequency (only occurs once in each witness) and the largest length.
    # Larger continuous blocks are more rare.
    max = df['uniqueness'].max()
    most_unique_blocks = df.loc[df['uniqueness'] == max]
    print(max)
    print(most_unique_blocks)
    return most_unique_blocks


# returns matches as (instance -> instance from the token_index)
def potential_instance_to_instance_matches(token_index, witness):
    instances = token_index.block_instances_for_witness(witness)
    # print("> token_index.witness_to_block_instances", token_index.witness_to_block_instances)
    # print("> instances", instances)
    start_token_position_for_witness = token_index.start_token_position_for_witness(witness)
    # print("> start_token_position_for_witness=", start_token_position_for_witness)
    return potential_instance_to_instance_matches_for_specific_instances(instances, start_token_position_for_witness)


def potential_instance_to_instance_matches_for_specific_instances(instances, start_token_position_for_witness):
    matches = []
    for instance_in_witness in instances:
        # print("> witness_instance=", instance_in_witness)
        block = instance_in_witness.block
        all_instances = block.get_all_instances()
        instances_in_graph = [i for i in all_instances if i.start_token < start_token_position_for_witness]
        for instance_in_graph in instances_in_graph:
            match = BlockInstanceToBlockInstanceMatch(instance_in_graph, instance_in_witness)
            matches.append(match)
    return matches


class BlockInstanceToBlockInstanceMatch(object):
    def __init__(self, block_instance1, block_instance2):
        self.block_instance1 = block_instance1
        self.block_instance2 = block_instance2

    def __repr__(self):
        return str.format("Match(instance1={},instance2={})", self.block_instance1, self.block_instance2)


# old token to token matches
# returns matches as (index -> index in the token array mapping)
def potential_token_to_token_matches(token_index, witness):
    instances = token_index.block_instances_for_witness(witness)
    # print("> token_index.witness_to_block_instances", token_index.witness_to_block_instances)
    # print("> instances", instances)
    start_token_position_for_witness = token_index.start_token_position_for_witness(witness)
    # print("> start_token_position_for_witness=", start_token_position_for_witness)
    return potential_token_to_token_matches_for_specific_instances(instances, start_token_position_for_witness)
    pass


# This could be written as a function of potential_instance to instances etc.
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



