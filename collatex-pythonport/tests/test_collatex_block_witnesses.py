'''
Created on Apr 27, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from tests import unit_disabled
from ClusterShell.RangeSet import RangeSet
from array import array
from collatex import Collation
from collatex.extended_suffix_array import ExtendedSuffixArray, Block
from collatex.suffix_based_scorer import Scorer




class Test(unittest.TestCase):
    def assertIntervalIn(self, start, length, nr_of_occurrences, intervals):
        found = False
        for lcp_interval in intervals:
            if lcp_interval.token_start_position == start and lcp_interval.minimum_block_length == length and lcp_interval.number_of_occurrences == nr_of_occurrences:
                found = True
                break 
        if not found:
            self.fail("Interval with "+str(start)+" and "+str(length)+" and "+str(nr_of_occurrences)+" not found in "+str(intervals))
    
# TODO: re-enable test!
    # Note: LCP intervals can overlap
    @unit_disabled
    def test_lcp_intervals_failing_use_case_old_algorithm(self):
        collation = Collation()
        collation.add_plain_witness("W1", "the cat and the dog")
        collation.add_plain_witness("W2", "the dog and the cat")
        parent_lcp_intervals, child_lcp_intervals = collation.get_lcp_intervals()
        self.assertIn((1,2), parent_lcp_intervals)
        self.assertIn((3,4), parent_lcp_intervals)
        self.assertIn((5,6), parent_lcp_intervals)
        self.assertIn((7, 10), parent_lcp_intervals)
        self.assertIn((7,8), child_lcp_intervals[7])
        self.assertIn((9,10), child_lcp_intervals[7])
    
# TODO: re-enable test!
    @unit_disabled
    def test_lcp_child_intervals_darwin(self):
        lcp_array = array('i', [0, 0, 0, 96, 96, 0, 151, 9, 1, 105, 105, 0, 83, 83, 0, 95, 95, 0, 39, 39, 0, 24, 24, 0, 108, 1, 0, 232, 32, 0, 181, 39, 0, 185, 43, 1, 33, 33, 0, 159, 17, 0, 160, 18, 0, 106, 106, 0, 60, 60, 0, 171, 29, 1, 215, 15, 1, 122, 15, 1, 57, 57, 1, 153, 11, 1, 165, 23, 0, 9, 9, 1, 170, 28, 0, 214, 14, 0, 62, 62, 1, 191, 49, 0, 61, 61, 0, 148, 6, 1, 8, 8, 0, 19, 19, 0, 123, 16, 0, 75, 75, 1, 90, 90, 1, 28, 28, 0, 167, 25, 1, 112, 5, 1, 132, 25, 0, 50, 50, 0, 31, 31, 0, 77, 77, 0, 97, 97, 0, 6, 6, 0, 38, 38, 0, 63, 63, 0, 30, 30, 0, 80, 80, 0, 154, 12, 0, 145, 3, 0, 129, 22, 3, 67, 67, 0, 88, 88, 0, 45, 45, 0, 217, 17, 1, 22, 22, 0, 166, 24, 0, 25, 25, 0, 201, 1, 0, 155, 13, 1, 120, 13, 0, 175, 33, 0, 195, 53, 0, 135, 28, 0, 10, 10, 0, 147, 5, 0, 138, 31, 0, 161, 19, 1, 73, 73, 0, 198, 1, 0, 86, 86, 0, 74, 74, 1, 111, 4, 0, 210, 10, 0, 84, 84, 0, 42, 42, 0, 199, 2, 0, 119, 12, 0, 46, 46, 1, 202, 2, 0, 71, 71, 0, 40, 40, 0, 142, 0, 0, 52, 52, 0, 168, 26, 2, 113, 6, 1, 163, 21, 0, 133, 26, 0, 3, 3, 0, 186, 44, 1, 101, 101, 0, 193, 51, 2, 227, 27, 0, 107, 1, 37, 37, 1, 140, 33, 0, 1, 205, 5, 0, 47, 47, 0, 127, 20, 1, 65, 65, 0, 230, 30, 0, 41, 41, 0, 91, 91, 0, 1, 1, 0, 200, 3, 0, 156, 14, 0, 0, 76, 76, 0, 109, 2, 1, 182, 40, 0, 68, 68, 1, 14, 14, 0, 126, 19, 0, 34, 34, 1, 192, 50, 1, 85, 85, 1, 128, 21, 2, 66, 66, 1, 183, 41, 1, 220, 20, 1, 5, 5, 1, 212, 12, 2, 174, 32, 2, 226, 26, 1, 59, 59, 0, 16, 16, 0, 218, 18, 0, 23, 23, 1, 11, 11, 0, 36, 36, 1, 178, 36, 0, 51, 51, 0, 213, 13, 1, 190, 48, 0, 2, 2, 1, 222, 22, 1, 188, 46, 0, 78, 78, 0, 53, 53, 0, 197, 0, 0, 136, 29, 1, 219, 19, 1, 12, 12, 0, 114, 7, 0, 89, 89, 0, 172, 30, 2, 216, 16, 0, 21, 21, 0, 209, 9, 0, 81, 81, 0, 102, 102, 0, 134, 27, 0, 98, 98, 0, 131, 24, 0, 4, 4, 0, 35, 35, 0, 179, 37, 0, 224, 24, 0, 82, 82, 0, 72, 72, 0, 139, 32, 0, 125, 18, 0, 103, 103, 0, 121, 14, 0, 189, 47, 0, 184, 42, 0, 7, 7, 1, 17, 17, 0, 207, 7, 0, 221, 21, 0, 20, 20, 0, 196, 54, 0, 79, 79, 1, 204, 4, 1, 144, 2, 2, 94, 94, 1, 56, 56, 0, 211, 11, 1, 194, 52, 3, 228, 28, 1, 157, 15, 1, 69, 69, 1, 54, 54, 1, 115, 8, 1, 173, 31, 1, 225, 25, 1, 177, 35, 1, 100, 100, 0, 203, 3, 0, 150, 8, 0, 104, 104, 0, 143, 1, 1, 93, 93, 0, 118, 11, 0, 29, 29, 1, 64, 64, 1, 146, 4, 1, 137, 30, 1, 229, 29, 2, 70, 70, 1, 44, 44, 1, 49, 49, 1, 117, 10, 0, 152, 10, 0, 130, 23, 1, 26, 26, 1, 110, 3, 1, 158, 16, 0, 124, 17, 0, 206, 6, 0, 141, 34, 1, 92, 92, 0, 32, 32, 1, 27, 27, 0, 58, 58, 0, 162, 20, 0, 13, 13, 0, 187, 45, 1, 223, 23, 0, 43, 43, 0, 48, 48, 0, 176, 34, 0, 99, 99, 0, 149, 7, 1, 231, 31, 1, 180, 38, 0, 18, 18, 0, 55, 55, 0, 169, 27, 2, 164, 22, 1, 208, 8, 1, 116, 9, 0, 87, 87, 0, 15, 15])
        collation = Collation()
        _, child_lcp_intervals = collation.get_lcp_intervals(lcp=lcp_array)
        self.assertEqual([(5, 7), (8, 10)], child_lcp_intervals[5])
        self.assertEqual([(513, 515),(516, 518),(519, 521),(522, 524),(525,527)], child_lcp_intervals[513])

    @unit_disabled
    def test_non_overlapping_blocks_black_cat(self):
        collation = Collation()
        collation.add_plain_witness("W1", "the black cat")
        collation.add_plain_witness("W2", "the black cat")
        algorithm = Scorer(collation)
        blocks = algorithm._get_non_overlapping_repeating_blocks()
        block1 = Block(RangeSet("0-2, 4-6"))
        self.assertEqual([block1], blocks)

    #TODO: Fix number of siblings!
    @unit_disabled
    def test_blocks_failing_transposition_use_case_old_algorithm(self):
        collation = Collation()
        collation.add_plain_witness("W1", "the cat and the dog")
        collation.add_plain_witness("W2", "the dog and the cat")
        algorithm = Scorer(collation)
        blocks = algorithm._get_non_overlapping_repeating_blocks()
        block1 = Block(RangeSet("0-1, 9-10"))
        block2 = Block(RangeSet("3-4, 6-7"))
        block3 = Block(RangeSet("2, 8"))
        self.assertEqual([block1, block2, block3], blocks)

    # In the new approach nothing should be split
    @unit_disabled
    def test_blocks_splitting_token_case(self):
        collation = Collation()
        collation.add_plain_witness("W1", "a c b c")
        collation.add_plain_witness("W2", "a c b")
        algorithm = Scorer(collation)
        blocks = algorithm._get_non_overlapping_repeating_blocks()
        block1 = Block(RangeSet("0-2, 5-7")) # a c b
        self.assertIn(block1, blocks)

    @unit_disabled
    def test_block_witnesses_Hermans_case_two_witnesses(self):
        collation = Collation()
        collation.add_plain_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_plain_witness("W2", "a b c d F g h i ! q r s t")
        algorithm = Scorer(collation)
        block_witness = algorithm._get_block_witness(collation.witnesses[0])
        self.assertEquals(["a b c d F g h i !", "q r s t"], block_witness.debug())
        block_witness = algorithm._get_block_witness(collation.witnesses[1])
        self.assertEquals(["a b c d F g h i !", "q r s t"], block_witness.debug())

    @unit_disabled
    def test_block_witnesses_Hermans_case(self):
        collation = Collation()
        collation.add_plain_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_plain_witness("W2", "a b c d F g h i ! q r s t")
        collation.add_plain_witness("W3", "a b c d E g h i ! q r s t")
        algorithm = Scorer(collation)
        block_witness1 = algorithm._get_block_witness(collation.witnesses[0])
        self.assertEquals(["a b c d", "F", "g h i", "! q r s t"], block_witness1.debug())
        block_witness2 = algorithm._get_block_witness(collation.witnesses[1])
        self.assertEquals(["a b c d", "F", "g h i", "! q r s t"], block_witness2.debug())
        block_witness3 = algorithm._get_block_witness(collation.witnesses[2])
        self.assertEquals(["a b c d", "g h i", "! q r s t"], block_witness3.debug())
        
    # LCP interval is not ascending nor descending
    @unit_disabled
    def test_split_lcp_intervals_into_smaller_intervals(self):
        collation = Collation()
        collation.add_plain_witness("W1", "the cat")
        collation.add_plain_witness("W2", "the cat")
        collation.add_plain_witness("W3", "the cat")
        extsufarr = collation.to_extended_suffix_array()
        split_intervals = extsufarr.split_lcp_array_into_intervals()
        self.assertIntervalIn(0, 2, 3, split_intervals) # the cat
        self.assertIntervalIn(1, 1, 3, split_intervals) # cat
        self.assertEqual(2, len(split_intervals), "More items: "+str(split_intervals))
        
    # LCP interval is ascending
    @unit_disabled
    def test_split_lcp_intervals_into_smaller_intervals_2(self):
        collation = Collation()
        collation.add_plain_witness("W1", "the")
        collation.add_plain_witness("W2", "the cat")
        collation.add_plain_witness("W3", "the cat sits")
        extsufarr = collation.to_extended_suffix_array()
        split_intervals = extsufarr.split_lcp_array_into_intervals()
        self.assertIntervalIn(0, 1, 3, split_intervals) # the
        self.assertIntervalIn(2, 2, 2, split_intervals) # the cat
        self.assertIntervalIn(3, 1, 2, split_intervals) # cat
        self.assertEqual(3, len(split_intervals), "More items: "+str(split_intervals))

    @unit_disabled
    def test_filter_potential_blocks(self):
        collation = Collation()
        collation.add_plain_witness("W1", "the fox jumps over the fox")
        collation.add_plain_witness("w2", "the fox jumps over the dog")
        potential_blocks = collation.calculate_potential_blocks()
        collation.filter_potential_blocks(potential_blocks)
        self.fail("TESTING!")

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()