'''
Created on Apr 7, 2014

@author: Ronald Haentjens Dekker
'''
import unittest
from linsuffarr import SuffixArray




class Test(unittest.TestCase):


    def testLineairSuffixArray(self):
        sa = SuffixArray("The Quick Brown Fox Jumped Over")
        print(sa)
        pass


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()