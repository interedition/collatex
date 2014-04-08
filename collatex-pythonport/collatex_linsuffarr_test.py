'''
Created on Apr 7, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from linsuffarr import SuffixArray
from collatex_simple import SuperMaximumRe




    
    
    



class Test(unittest.TestCase):


    def testLineairSuffixArray(self):
        sa = SuffixArray("The Quick Brown Fox Jumped Over")
        print(sa)
        pass

    def testSA2(self):
        # $ is meant to separate witnesses here
        sa = SuffixArray("a b c d F g h i ! K ! q r s t $ a b c d F g h i ! q r s t")
        print(sa)
        print(sa._LCP_values)
        
    def test_blocks(self):
        # $ is meant to separate witnesses here
        sa = SuffixArray("a b c d F g h i ! K ! q r s t $ a b c d F g h i ! q r s t")
        smr = SuperMaximumRe()
        blocks = smr.find_blocks(sa)
        print(blocks)

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()