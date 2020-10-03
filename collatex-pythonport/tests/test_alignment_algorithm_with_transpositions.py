"""
Created on Oct 3, 2020

@author: Ronald Haentjens Dekker
"""
import unittest

from collatex import Collation, collate


class Test(unittest.TestCase):
    collation = Collation()
    collation.add_plain_witness("w1", "the red and the black cat")
    collation.add_plain_witness("w2", "the black and the red cat")
    alignment_table = collate(collation)
    print(alignment_table)

    # w1: the red and the black cat
    # w2: the black and the red cat

    pass


if __name__ == "__main__":
    # import sys;sys.argv = ['', 'Test.testName']
    unittest.main()