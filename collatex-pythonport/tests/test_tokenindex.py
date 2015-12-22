import unittest
from array import array

from ClusterShell.RangeSet import RangeSet
from collatex import Collation
from collatex.tokenindex import TokenIndex


class Test(unittest.TestCase):
    # helper method to assert LCP intervals
    def assertLCP_Interval(self, start, length, depth, numberOfTimes, lcp_interval):
        self.assertEquals(start, lcp_interval.start)
        self.assertEquals(length, lcp_interval.length)
        self.assertEquals(depth, lcp_interval.number_of_witnesses)
        self.assertEquals(numberOfTimes, lcp_interval.number_of_occurrences)

    # helper method to assert LCP intervals
    def assertIntervalIn(self, start, length, nr_of_occurrences, intervals):
        found = False
        for lcp_interval in intervals:
            if lcp_interval.token_start_position == start and lcp_interval.minimum_block_length == length and lcp_interval.number_of_occurrences == nr_of_occurrences:
                found = True
                break
        if not found:
            self.fail("Interval with " + str(start) + " and " + str(length) + " and " + str(
                nr_of_occurrences) + " not found in " + str(intervals))


    def assertTokenArray(self, expected, token_index):
        self.assertEquals(expected, " ".join(str(token) for token in token_index.token_array))


    def test_token_array_hermans_case(self):
        collation = Collation()
        collation.add_plain_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_plain_witness("W2", "a b c d F g h i ! q r s t")
        token_index = TokenIndex(collation.witnesses)
        token_index.prepare()
        # $ is meant to separate witnesses here
        self.assertTokenArray("a b c d F g h i ! K ! q r s t $0 a b c d F g h i ! q r s t", token_index)

    def testTokenArrayMarkersWithThreeWitnesses(self):
        collation = Collation()
        collation.add_plain_witness("W1", "interesting nice huh")
        collation.add_plain_witness("W2", "very nice right")
        collation.add_plain_witness("W3", "especially interesting")
        token_index = TokenIndex(collation.witnesses)
        token_index.prepare()
        self.assertTokenArray("interesting nice huh $0 very nice right $1 especially interesting", token_index)

    # test whether the witness->range mapping works
    def test_witness_ranges_hermans_case(self):
        collation = Collation()
        collation.add_plain_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_plain_witness("W2", "a b c d F g h i ! q r s t")
        token_index = TokenIndex(collation.witnesses)
        token_index.prepare()
        self.assertEquals(RangeSet("0-14"), token_index.get_range_for_witness("W1"))
        self.assertEquals(RangeSet("16-28"), token_index.get_range_for_witness("W2"))

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
        self.assertLCP_Interval(2, 1, 3, 3, blocks[0])  # a
        self.assertLCP_Interval(5, 1, 2, 2, blocks[1])  # b
        self.assertLCP_Interval(7, 2, 2, 2, blocks[2])  # c d
        self.assertLCP_Interval(9, 1, 3, 3, blocks[3])  # d
        self.assertLCP_Interval(12, 1, 2, 2, blocks[4])  # e
        self.assertEquals(5, len(blocks))

    def test_split_lcp_intervals_ascending_descending_ascending(self):
        lcp_array = array('i', [0, 4, 143, 87, 1, 1, 12, 93, 93, 37])
        sa_array = array('i', [0, 1, 2, 3, 4, 5, 6, 7, 8, 9])  # FAKED!
        token_index = TokenIndex.for_test(sa_array, lcp_array)
        split_intervals = token_index.split_lcp_array_into_intervals()
        self.assertIntervalIn(1, 143, 2, split_intervals)
        self.assertIntervalIn(1, 87, 3, split_intervals)
        self.assertIntervalIn(0, 4, 4, split_intervals)
        self.assertIntervalIn(6, 93, 3, split_intervals)
        self.assertIntervalIn(0, 1, 10, split_intervals)
        self.assertIntervalIn(5, 12, 5, split_intervals)
        self.assertIntervalIn(6, 37, 4, split_intervals)

    # LCP interval is first ascending, then descending
    def test_split_lcp_intervals_ascending_then_descending_LCP(self):
        lcp_array = array('i', [0, 10, 149, 93, 7, 1])
        sa_array = array('i', [0, 1, 2, 3, 4, 5]) # FAKED!
        token_index = TokenIndex.for_test(sa_array, lcp_array)
        split_intervals = token_index.split_lcp_array_into_intervals()
        self.assertIntervalIn(0, 10, 4, split_intervals)
        self.assertIntervalIn(1, 149, 2, split_intervals)
        self.assertIntervalIn(1, 93, 3, split_intervals)
        self.assertIntervalIn(0, 7, 5, split_intervals)
        self.assertIntervalIn(0, 1, 6, split_intervals)
        self.assertEqual(5, len(split_intervals), "More items: "+str(split_intervals))

    # LCP interval is descending
    def test_split_lcp_intervals_descending_LCP(self):
        lcp_array = array('i', [0, 20, 20, 20, 4])
        sa_array = array('i', [0, 1, 2, 3, 4]) # FAKED!
        token_index = TokenIndex.for_test(sa_array, lcp_array)
        split_intervals = token_index.split_lcp_array_into_intervals()
        self.assertIntervalIn(0, 20, 4, split_intervals)
        self.assertIntervalIn(0, 4, 5, split_intervals)
        self.assertEqual(2, len(split_intervals), "More items: "+str(split_intervals))

    def test_lcp_intervals_number_of_witnesses_Hermans_case(self):
        collation = Collation()
        collation.add_plain_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_plain_witness("W2", "a b c d F g h i ! q r s t")
        collation.add_plain_witness("W3", "a b c d E g h i ! q r s t")
        token_index = TokenIndex(collation.witnesses)
        token_index.prepare()
        intervals = token_index.split_lcp_array_into_intervals()
        potential_block = intervals[1] # ! q r s t
        self.assertEqual(3, potential_block.number_of_witnesses)

    # rename test, test does nothing regarding filtering
    def test_filter_potential_blocks(self):
        collation = Collation()
        collation.add_plain_witness("W1", "a a")
        collation.add_plain_witness("w2", "a")
        token_index = TokenIndex(collation.witnesses)
        token_index.prepare()
        intervals = token_index.split_lcp_array_into_intervals()
        # expectations
        # There is one interval with length 1, number of occurrences 3, number of witnesses: 2
        a_interval = intervals[0] # a
        self.assertEqual(2, a_interval.number_of_witnesses)
        self.assertEqual(1, a_interval.length)
        self.assertEqual(3, a_interval.number_of_occurrences)


