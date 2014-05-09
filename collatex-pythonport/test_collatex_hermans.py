'''
Created on Apr 10, 2014

@author: Ronald Haentjens Dekker
'''

import unittest
from ClusterShell.RangeSet import RangeSet
from collatex_suffix import Collation, Block


class Test(unittest.TestCase):
    def test_Hermans_non_overlapping_blocks(self):
        collation = Collation()
        collation.add_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_witness("W2", "a b c d F g h i ! q r s t")
        blocks = collation.get_non_overlapping_repeating_blocks()
        block1 = Block(RangeSet("8, 10, 24")) # !
        block2 = Block(RangeSet("0-7, 16-23")) # a b c d F g h i
        block3 = Block(RangeSet("11-14, 25-28")) # q r s t
        self.assertEquals([block1, block2, block3], blocks)
    
    def test_Hermans_case_blocks_three_witnesses(self):
        collation = Collation()
        collation.add_witness("W1", "a b c d F g h i ! K ! q r s t")
        collation.add_witness("W2", "a b c d F g h i ! q r s t")
        collation.add_witness("W3", "a b c d E g h i ! q r s t")
        blocks = collation.get_non_overlapping_repeating_blocks()
        block1 = Block(RangeSet("8, 10, 24, 38")) # !
        block2 = Block(RangeSet("0-3, 16-19, 30-33")) # a b c d
        block3 = Block(RangeSet("5-7, 21-23, 35-37")) # g h i
        block4 = Block(RangeSet("11-14, 25-28, 39-42")) # q r s t
        block5 = Block(RangeSet("4, 20")) # F
        self.assertEquals([block1, block2, block3, block4, block5], blocks)

    
    
#     def test_Hermans_case_blocks(self):
#         collation = Collation()
#         collation.add_witness("W1", "a b c d F g h i ! K ! q r s t")
#         collation.add_witness("W2", "a b c d F g h i ! q r s t")
#         # $ is meant to separate witnesses here
#         # TODO: Re-enable this later! Tests at the moments returns $3
#         # self.assertEquals("a b c d F g h i ! K ! q r s t $1 a b c d F g h i ! q r s t", collation.get_combined_string())
#         blocks = collation.get_blocks()
#         # we expect two blocks ("a b c d F g h i !", "q r s t")
#         # both numbers are inclusive
#         block1 = Block(RangeSet("0-8, 16-24"))
#         block2 = Block(RangeSet("11-14, 25-28"))
#         #print(blocks)
#         print(collation.get_lcp_array())
#         self.assertEqual([block1, block2], blocks)
#      
         
            

    
    
    
    
    

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()