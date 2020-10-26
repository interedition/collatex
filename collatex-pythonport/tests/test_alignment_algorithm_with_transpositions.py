"""
Created on Oct 3, 2020

@author: Ronald Haentjens Dekker
"""
import unittest
from typing import List

import pandas as pd

from collatex import Collation
from collatex.collation_with_transposition import collate_with_transposition, potential_token_to_token_matches


class Test(unittest.TestCase):
    # w1: the red and the black cat
    # w2: the black and the red cat
    collation = Collation()
    collation.add_plain_witness("w1", "the red and the black cat")
    collation.add_plain_witness("w2", "the black and the red cat")
    token_index = collate_with_transposition(collation)
    # print(token_index.blocks)

    # a token index has blocks.
    # blocks have frequency etc.
    # a block has instances.
    # instances have tokens.

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
    print(max)
    print(df.loc[df['uniqueness'] == max])

    # we need to convert the intervals into matches
    # Then do the transposition detection
    # hmm I need the block index in that dataframe.
    # We need to get all the instances for a witness.
    # Then we filter so that we only take them if they are part of the important blocks.

    # We ask for all the potential matches (warning this is a lot)
    x = potential_token_to_token_matches(token_index, collation.witnesses[1])
    print(x)
    pass


if __name__ == "__main__":
    # import sys;sys.argv = ['', 'Test.testName']
    unittest.main()

    # NOTES:
    # I already have code in the Java and the Python version to convert a variant graph, a token index
    # and a given witness into a set of matches.
    # That code is in the Match Cube class in the Edit_graph_aligner file.
    # A match is currently defined as a (vertex, token) combination.
    # the vertex array is a token array -> vertex mapping for easy access into the variant graph.
    # the vertex ranking is a number assigned to each vertex. This can change after an addition to the graph.

    # after that we have to sort the matches twice, once by witness a order, tie -> witness b order.
    # and once by witness b order, tie -> witness a order.

    # This calls the traditional NeedlemannWunsch based alignment.
    # alignment_table = collate(collation)
    # print(alignment_table)

    # There is this annying cross between going witness by witness and working with the most unique blocks first.
    # workaround
    # We need to convert the blocks to instances in the second witness
    # We take the most prominent blocks
    # and take the witness 2 instances
    # We also need to create the token to vertex array
    # By creating a VG and merging the first witness in.
