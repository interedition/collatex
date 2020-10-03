"""
Created on Oct 3, 2020

@author: Ronald Haentjens Dekker
"""
import unittest

from collatex import Collation, collate
from collatex.collation_with_transposition import collate_with_transposition


class Test(unittest.TestCase):
    # w1: the red and the black cat
    # w2: the black and the red cat
    collation = Collation()
    collation.add_plain_witness("w1", "the red and the black cat")
    collation.add_plain_witness("w2", "the black and the red cat")
    token_index = collate_with_transposition(collation)
    print(token_index.blocks)

    # This calls the traditional NeedlemannWunsch based alignment.
    # alignment_table = collate(collation)
    # print(alignment_table)


    pass


if __name__ == "__main__":
    # import sys;sys.argv = ['', 'Test.testName']
    unittest.main()