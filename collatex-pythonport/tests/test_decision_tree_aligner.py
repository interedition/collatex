'''
Created on Aug 4, 2014

@author: ronald
'''
import unittest
from collatex.collatex_core import Witness
from collatex.aligner import Aligner


class Test(unittest.TestCase):


    def testOmission(self):
        a = Witness("A", "a b c")
        b = Witness("B", "b c")
        aligner = Aligner(a, b)
        aligner.align()
        
        pass


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testOmission']
    unittest.main()