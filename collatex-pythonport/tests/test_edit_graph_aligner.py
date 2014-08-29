'''
Created on Aug 4, 2014

@author: Ronald
'''
import unittest
from collatex.collatex_core import Witness
from collatex.edit_graph_aligner import EditGraphAligner


class Test(unittest.TestCase):


    def assertRow(self, expected, cell_data):
        actual = []
        for cell in cell_data:
            actual.append(cell.g)
        self.assertEqual(expected, actual)
        
    def debugRowSegments(self, cell_data):
        actual = []
        for cell in cell_data:
            actual.append(cell.segments)
        print(actual)
    
    def debug_table(self, aligner, table):
        for y in range(aligner.length_witness_b+1):
            for x in range(aligner.length_witness_a+1):
                print (y, x), table[y][x]

    # we need to introduce a gap here
    def testOmission(self):
        a = Witness("A", "a b c")
        b = Witness("B", "b c")
        aligner = EditGraphAligner(a, b)
        aligner.align()
        table = aligner.table
#         self.debug_table(aligner, table)
        self.assertEqual(0, table[0][0].g)
        self.assertEqual(-1, table[0][1].g)
        self.assertEqual(-2, table[0][2].g)
        self.assertEqual(-3, table[0][3].g)
        self.assertEqual(-1, table[1][0].g)
        self.assertEqual(-2, table[1][1].g)
        self.assertEqual(-1, table[1][2].g)
        self.assertEqual(-2, table[1][3].g)
        self.assertEqual(-2, table[2][0].g)
        self.assertEqual(-3, table[2][1].g)
        self.assertEqual(-2, table[2][2].g)
        self.assertEqual(-1, table[2][3].g)

    # test number of segments (we want the solution that returns only one segment)

    
    
    def testOmission2GlobalScore(self):
        a = Witness("A", "a a b c")
        b = Witness("B", "a b c")
        aligner = EditGraphAligner(a, b)
        aligner.align()
        table = aligner.table

        self.assertRow([0, -1, -2, -3, -4], table[0])
        self.assertRow([-1, 0, -1, -2, -3], table[1])
        self.assertRow([-2, -1, -2, -1, -2], table[2])
        self.assertRow([-3, -2, -3, -2, -1], table[3])
        
#     def testOmission2SegmentsScore(self):
#         a = Witness("A", "a a b c")
#         b = Witness("B", "a b c")
#         aligner = EditGraphAligner(a, b)
#         aligner.align()
#         table = aligner.table
# 
#         self.debugRowSegments(table[0])
#         self.debugRowSegments(table[1])
            
#      TODO: add test for segments
#         self.assertEqual(1, table[3][4].segments)
        
        
        
if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testOmission']
    unittest.main()
