'''
Created on Aug 4, 2014

@author: ronald
'''
import unittest
from tests import unit_disabled
from collatex.core_classes import Witness
from collatex.experimental_astar_aligner import Aligner


class Test(unittest.TestCase):

    @unit_disabled
    def testOmission(self):
        a = Witness("A", "a b c")
        b = Witness("B", "b c")
        aligner = Aligner(a, b)
        aligner.align()
        
        pass


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testOmission']
    unittest.main()