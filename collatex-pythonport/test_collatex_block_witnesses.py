'''
Created on Apr 27, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from collatex_suffix import Collation, Block
from ClusterShell.RangeSet import RangeSet

class Test(unittest.TestCase):

    def test_black_cat_non_overlapping_blocks(self):
        collation = Collation()
        collation.add_witness("W1", "the black cat")
        collation.add_witness("W2", "the black cat")
        blocks = collation.get_non_overlapping_repeating_blocks()
        block1 = Block(RangeSet("0-2, 4-6"))
        self.assertEqual([block1], blocks)

    # Note: LCP intervals can overlap
    def test_failing_use_case_old_algorithm_lcp_intervals(self):
        collation = Collation()
        collation.add_witness("W1", "the cat and the dog")
        collation.add_witness("W2", "the dog and the cat")
        parent_lcp_intervals, child_lcp_intervals = collation.get_lcp_intervals()
        self.assertIn((1,2), parent_lcp_intervals) 
        self.assertIn((3,4), parent_lcp_intervals)
        self.assertIn((5,6), parent_lcp_intervals)
        self.assertIn((7, 10), parent_lcp_intervals)
        self.assertIn((7,8), child_lcp_intervals[7])
        self.assertIn((9,10), child_lcp_intervals[7])
        
    def test_blocks_failing_transposition_use_case_old_algorithm(self):
        collation = Collation()
        collation.add_witness("W1", "the cat and the dog")
        collation.add_witness("W2", "the dog and the cat")
        blocks = collation.get_non_overlapping_repeating_blocks()
        block1 = Block(RangeSet("0-1, 9-10"))
        block2 = Block(RangeSet("3-4, 6-7"))
        block3 = Block(RangeSet("2, 8"))
        self.assertEqual([block1, block2, block3], blocks)

    def test_Hermans_case_block_witnesses(self):
        collation = Collation()
        collation.add_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_witness("W2", "a b c d F g h i ! q r s t")
        collation.add_witness("W3", "a b c d E g h i ! q r s t")
        block_witness = collation.get_block_witness(collation.witnesses[0])
        self.assertEquals(["a b c d", "F", "g h i", "!", "!", "q r s t"], block_witness.debug())
        block_witness = collation.get_block_witness(collation.witnesses[1])
        self.assertEquals(["a b c d", "F", "g h i", "!", "q r s t"], block_witness.debug())
        block_witness = collation.get_block_witness(collation.witnesses[2])
        self.assertEquals(["a b c d", "g h i", "!", "q r s t"], block_witness.debug())

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()