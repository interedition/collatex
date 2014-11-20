'''
Created on Aug 4, 2014

@author: Ronald
'''
import unittest
from tests import unit_disabled
from collatex.core_functions import VariantGraph
from collatex.edit_graph_aligner import EditGraphAligner
from collatex import Collation


class Test(unittest.TestCase):

    # global score
    def assertRow(self, expected, cell_data):
        actual = []
        for cell in cell_data:
            actual.append(cell.g)
        self.assertEqual(expected, actual)
    
    def assertSuperbaseEquals(self, expected, superbase):
        actual = ""
        for token in superbase:
            if actual:
                actual += " "
            actual += str(token)
        self.assertEquals(expected, actual)    
    
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
        collation = Collation()
        collation.add_plain_witness("A", "a b c")
        collation.add_plain_witness("B", "b c")
        aligner = EditGraphAligner(collation)
        graph = VariantGraph()
        aligner.collate(graph, collation)
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


    # note: the scoring table in this test is only correct when block detection is OFF
    @unit_disabled
    def testOmission2GlobalScore(self):
        collation = Collation()
        collation.add_plain_witness("A", "a a b c")
        collation.add_plain_witness("B", "a b c")
        aligner = EditGraphAligner(collation)
        graph = VariantGraph()
        aligner.collate(graph, collation)
        table = aligner.table
 
        self.assertRow([0, -1, -2, -3, -4], table[0])
        self.assertRow([-1, 0, -1, -2, -3], table[1])
        self.assertRow([-2, -1, -2, -1, -2], table[2])
        self.assertRow([-3, -2, -3, -2, -1], table[3])


    def test_superbase(self):
        collation = Collation()
        collation.add_plain_witness("A", "X a b c d e f X g h i Y Z j k")
        collation.add_plain_witness("B", "a b c Y d e f Y Z g h i X j k")
        aligner = EditGraphAligner(collation)
        graph = VariantGraph()
        aligner.collate(graph, collation)
        superbase = aligner.new_superbase
        self.assertSuperbaseEquals("X a b c Y d e f X Y Z g h i Y Z X j k", superbase)
        
        
        # TODO: add Y to the witness B (to check end modification
        
        
#     def test_path(self):
#         a = Witness("A", "a b c")
#         b = Witness("B", "a b c")
#         aligner = EditGraphAligner(a, b)
#         aligner.align()
#         segments = aligner.get_segments()
#         self.assertSegments(["a b c"], segments)
        
        
#         path = aligner.get_path()
#         self.assertPath([(0,0),(1,1),(2,2),(3,3)], path)
        
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
