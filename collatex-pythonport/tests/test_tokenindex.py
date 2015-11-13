import unittest
from array import array

from collatex.tokenindex import TokenIndex
from collatex.extended_suffix_array import Block
from ClusterShell.RangeSet import RangeSet
from collatex.suffix_based_scorer import Scorer
from collatex import Collation

class Test(unittest.TestCase):

    # helper method to assert LCP intervals
    def assertLCP_Interval(self, start, length, depth, numberOfTimes, lcp_interval):
        self.assertEquals(start, lcp_interval.start)
        self.assertEquals(length, lcp_interval.length)
        self.assertEquals(depth, lcp_interval.number_of_witnesses)
        self.assertEquals(numberOfTimes, lcp_interval.number_of_occurrences)

    # ik heb drie witnesses nodig om de bug aan te tonen, met 1 gaat het al goed
    def testTokenArray(self):
        collation = Collation()
        collation.add_plain_witness("W1", "interesting nice huh")
        collation.add_plain_witness("W2", "very nice right")
        collation.add_plain_witness("W3", "especially interesting")
        token_index = TokenIndex(collation.witnesses)
        token_index.prepare()
        self.assertEquals("[interesting, nice, huh, $0, very, nice, right, $1, especially, interesting]", str(token_index.token_array))


    # TODO: add suffix array test by asserting that the tokens are sorted correctly
    # can't asserts the numbers due to randomness




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
        self.assertEquals(array('i', [0, 0, 0, 1, 1, 0, 1, 0, 2, 0, 1, 1, 0, 1]), token_index.get_lcp_array())

    def testCaseDanielStoeklLCPIntervals(self):
        collation = Collation()
        collation.add_plain_witness("W1", "a b c d e")
        collation.add_plain_witness("W2", "a e c d")
        collation.add_plain_witness("W3", "a d b")
        token_index = TokenIndex(collation.witnesses)
        token_index.prepare()
        blocks = token_index.split_lcp_array_into_intervals()
        self.assertLCP_Interval(2, 1, 3, 3, blocks[0]) # a
        self.assertLCP_Interval(5, 1, 2, 2, blocks[1]) # b
        self.assertLCP_Interval(7, 2, 2, 2, blocks[2]) # c d
        self.assertLCP_Interval(9, 1, 3, 3, blocks[3]) # d
        self.assertLCP_Interval(12, 1, 2, 2, blocks[4]) # e
        self.assertEquals(5, len(blocks))




