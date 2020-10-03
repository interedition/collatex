"""
Created on Oct 3, 2020

@author: Ronald Haentjens Dekker
"""
import unittest
from typing import List

import pandas as pd

from collatex import Collation, collate
from collatex.collation_with_transposition import collate_with_transposition


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
    for block in token_index.blocks:
        blocks_as_list.append([repr(block.get_all_instances()[0]), block.get_frequency(), block.length, block.get_depth()])
        pass

    print(blocks_as_list)

    df = pd.DataFrame([[4, 7, 10], [5, 8, 11],
                       [6, 9, 12]],
                      index=[1, 2, 3], columns=['a', 'b', 'c'])

    print(df)

    # This calls the traditional NeedlemannWunsch based alignment.
    # alignment_table = collate(collation)
    # print(alignment_table)


    pass


if __name__ == "__main__":
    # import sys;sys.argv = ['', 'Test.testName']
    unittest.main()