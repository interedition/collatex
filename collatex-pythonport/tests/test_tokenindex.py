import unittest
from array import array

from collatex.TokenIndex import TokenIndex
from collatex.extended_suffix_array import Block
from ClusterShell.RangeSet import RangeSet
from collatex.suffix_based_scorer import Scorer
from collatex import Collation

class Test(unittest.TestCase):

    def testCaseDanielStoekl(self):
        collation = Collation()
        collation.add_plain_witness("W1", "a b c d e")
        collation.add_plain_witness("W2", "a e c d")
        collation.add_plain_witness("W3", "a d b")
        token_index = TokenIndex(collation.witnesses)
        token_index.prepare()
        # Note: the suffix array can have multiple forms
        # outcome of sorting is not guaranteed
        # however the LCP array is fixed we can assert that
        self.assertEquals(array('i', [0, 2, 0, 1, 1, 0, 1, 0, 2, 0, 1, 1, 0, 1]), token_index.get_lcp_array())



