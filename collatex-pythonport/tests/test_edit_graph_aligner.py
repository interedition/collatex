'''
Created on Aug 4, 2014

@author: ronald
'''
import unittest
from collatex.collatex_core import Witness
from collatex.edit_graph_aligner import EditGraphAligner


class Test(unittest.TestCase):



    def debug_table(self, aligner, table):
        for y in range(aligner.length_witness_b):
            for x in range(aligner.length_witness_a):
                print (y, x), table[y][x]

    # we need to introduce a gap here
    def testOmission(self):
        a = Witness("A", "a b c")
        b = Witness("B", "b c")
        aligner = EditGraphAligner(a, b)
        aligner.align()
        table = aligner.table
#         self.debug_table(aligner, table)
        self.assertEqual(-1, table[0][0].g)
        self.assertEqual(-1, table[0][1].g)
        self.assertEqual(-2, table[0][2].g)
        self.assertEqual(-2, table[1][0].g)
        self.assertEqual(-2, table[1][1].g)
        self.assertEqual(-1, table[1][2].g)
        