import unittest
from collatex import Collation

__author__ = 'ronalddekker'

class Test(unittest.TestCase):

    def test_lcp_intervals_number_of_witnesses_Hermans_case(self):
        collation = Collation()
        collation.add_plain_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_plain_witness("W2", "a b c d F g h i ! q r s t")
        collation.add_plain_witness("W3", "a b c d E g h i ! q r s t")
        extsufarr = collation.to_extended_suffix_array()
        intervals = extsufarr.split_lcp_array_into_intervals()
        potential_block = intervals[1] # ! q r s t
        self.assertEqual(3, potential_block.number_of_witnesses)

    def test_filter_potential_blocks(self):
        collation = Collation()
        collation.add_plain_witness("W1", "a a")
        collation.add_plain_witness("w2", "a")
        extsufarr = collation.to_extended_suffix_array()
        intervals = extsufarr.split_lcp_array_into_intervals()
        # expectations
        # There is one interval with length 1, number of occurrences 3, number of witnesses: 2
        a_interval = intervals[0] # a
        self.assertEqual(2, a_interval.number_of_witnesses)
        self.assertEqual(1, a_interval.length)
        self.assertEqual(3, a_interval.number_of_occurrences)









