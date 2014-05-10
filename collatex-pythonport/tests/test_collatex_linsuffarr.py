'''
Created on Apr 7, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from linsuffarr import SuffixArray
from ClusterShell.RangeSet import RangeSet
from collatex_suffix import SuperMaximumRe, Block, calculate_Burrows_Wheeler_transform
from collatex_core import Tokenizer

class Test(unittest.TestCase):

#     #TODO: write asserts
#     def testLineairSuffixArray(self):
#         sa = SuffixArray("The Quick Brown Fox Jumped Over")
#         print(sa)
#         pass
# 
#     #TODO: write asserts
#     def testSA2(self):
#         # $ is meant to separate witnesses here
#         sa = SuffixArray("a b c d F g h i ! K ! q r s t $ a b c d F g h i ! q r s t")
#         print(sa)
#         print(sa._LCP_values)
        
    def test_blocks(self):
        # $ is meant to separate witnesses here
        sa = SuffixArray("a b c d F g h i ! K ! q r s t $ a b c d F g h i ! q r s t")
        smr = SuperMaximumRe()
        blocks = smr.find_blocks(sa)
        # we expect two blocks ("a b c d F g h i !", "q r s t")
        # both numbers are inclusive
        block1 = Block(RangeSet("0-8, 16-24"))
        block2 = Block(RangeSet("11-14, 25-28"))
        #print(blocks)
        self.assertEqual([block1, block2], blocks)

    def test_BWT(self):
        # $ is meant to end witness
        contents = "a p p e l l e e $"
        tokenizer = Tokenizer()
        tokens = tokenizer.tokenize(contents)
        sa = SuffixArray(contents)
        bwt = calculate_Burrows_Wheeler_transform(tokens, sa)
        # print(tokens)
        # print(sa)
        # print(bwt)
        self.assertEqual(['e', '$', 'e', 'l', 'p', 'l', 'e', 'p', 'a'], bwt)

    def test_BWT_2(self):
        # $ is meant to end witness
        contents = "b a n a n a $"
        tokenizer = Tokenizer()
        tokens = tokenizer.tokenize(contents)
        sa = SuffixArray(contents)
        bwt = calculate_Burrows_Wheeler_transform(tokens, sa)
        # print(tokens)
        # print(sa)
        # print(bwt)
        self.assertEqual(['a', 'n', 'n', 'b', '$', 'a', 'a'], bwt)
        
if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()