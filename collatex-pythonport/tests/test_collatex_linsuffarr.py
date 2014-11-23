'''
Created on Apr 7, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from collatex.linsuffarr import SuffixArray



class Test(unittest.TestCase):
    pass

    #TODO: write asserts
    def testLinearSuffixArray(self):
        sa = SuffixArray("The Quick Brown Fox Jumped Over")
        print(sa)
        pass

    #TODO: write asserts
    def testSA2(self):
        # $ is meant to separate witnesses here
        sa = SuffixArray("a b c d F g h i ! K ! q r s t $ a b c d F g h i ! q r s t")
        print(sa)
        print(sa._LCP_values)
        
if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()